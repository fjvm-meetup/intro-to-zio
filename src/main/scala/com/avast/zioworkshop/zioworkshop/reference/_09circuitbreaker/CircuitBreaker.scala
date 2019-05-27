package com.avast.zioworkshop.zioworkshop.reference._09circuitbreaker

import java.util.concurrent.TimeUnit

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{HttpRequest, HttpResponse, Pipeline, PipelineStage}
import org.http4s.Uri.Host
import scalaz.zio.duration.Duration
import scalaz.zio.{Ref, Task, UIO, ZIO}

/**
 * See for general idea: https://en.wikipedia.org/wiki/Circuit_breaker_design_pattern
 * This is NOT 3 minutes job. But you are welcome to try.
 * Hints:
 *    - scalaz.zio.Ref
 *    - Common.clock
 */
object CircuitBreaker {
  import Common.HttpDsl._

  sealed trait State
  object State {
    final case class Open(since: Long)     extends State
    final case object HalfOpen             extends State
    final case class Closed(failures: Int) extends State
  }

  type CircuitBreakers = Map[Host, State]

  private def stage(maxFailures: Int, timeout: Duration)(circuitBreakersRef: Ref[CircuitBreakers]): PipelineStage =
    new PipelineStage {
      override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
        withHost(next, request) { host =>
          Common.clock.nanoTime.flatMap { now =>
            circuitBreakersRef
              .modify[Task[HttpResponse]] { circuitBreakers =>
                circuitBreakers.get(host) match {
                  case None | Some(State.Closed(_)) =>
                    closed(next)(host, request) -> circuitBreakers
                  case Some(State.Open(since)) =>
                    val (step, state) = open(next)(host, now, since, request)
                    step -> circuitBreakers.updated(host, state)
                  case Some(State.HalfOpen) =>
                    BadGateway("Circuit breaker is opened.") -> circuitBreakers
                }
              }
          }.flatten
        }

      private def withHost(next: Pipeline, request: HttpRequest)(f: Host => Task[HttpResponse]): Task[HttpResponse] =
        request.uri.authority.map(_.host) match {
          case Some(host) =>
            f(host)
          case None =>
            Common.logger.warn(s"Can't find host for ${request.uri}") *>
              next.run(request)
        }

      private def responseBracket(
        acquire: UIO[Unit],
        handleFailure: UIO[Unit],
        handleSuccess: UIO[Unit],
        use: Task[HttpResponse]
      ) =
        acquire.bracketExit[Any, Throwable, HttpResponse](
          (_, exit) =>
            exit
              .foldM(_ => handleFailure, response => if (response.status.code >= 500) handleFailure else handleSuccess),
          _ => use
        )

      private def closed(next: Pipeline)(host: Host, request: HttpRequest): Task[HttpResponse] =
        responseBracket(ZIO.unit, closedFailure(host), closedSuccess(host), next.run(request))

      private def closedSuccess(host: Host): UIO[Unit] =
        circuitBreakersRef.update { circuitBreakers =>
          circuitBreakers.get(host) match {
            case Some(State.Closed(_)) | None => circuitBreakers - host
            case _                            => circuitBreakers
          }
        }.unit

      private def closedFailure(host: Host): UIO[Unit] =
        Common.clock.nanoTime.flatMap { now =>
          circuitBreakersRef.modify { circuitBreakers =>
            circuitBreakers.get(host) match {
              case None =>
                if (maxFailures == 1) {
                  Common.logger.info(s"circuit breaker: closed -> open for $host") -> circuitBreakers.updated(
                    host,
                    State.Open(now)
                  )
                } else {
                  ZIO.unit -> circuitBreakers.updated(host, State.Closed(1))
                }
              case Some(State.Closed(failures)) =>
                if (failures + 1 == maxFailures) {
                  Common.logger.info(s"circuit breaker: closed -> open for $host") -> circuitBreakers.updated(
                    host,
                    State.Open(now)
                  )

                } else {
                  ZIO.unit -> circuitBreakers.updated(host, State.Closed(failures + 1))
                }
              case _ => ZIO.unit -> circuitBreakers
            }
          }
        }.flatten

      private def open(
        next: Pipeline
      )(host: Host, now: Long, since: Long, request: HttpRequest): (Task[HttpResponse], State) =
        if (Duration.fromNanos(now - since) >= timeout) {
          val task = responseBracket(
            Common.logger
              .info(s"circuit breaker: open -> half-open for $host"),
            halfOpenFailure(host),
            halfOpenSuccess(host),
            next.run(request)
          )
          task -> State.HalfOpen
        } else {
          BadGateway("Circuit breaker is opened.") -> State.Open(since)
        }

      private def halfOpenSuccess(host: Host): UIO[Unit] =
        circuitBreakersRef.modify { circuitBreakers =>
          Common.logger.info(s"circuit breaker: half-open -> close for $host") -> (circuitBreakers - host)
        }.flatten *>
          circuitBreakersRef.get.map(_.toString()).flatMap(Common.logger.info)
      private def halfOpenFailure(host: Host): UIO[Unit] = Common.clock.nanoTime.flatMap { now =>
        circuitBreakersRef.modify { circuitBreaker =>
          Common.logger
            .info(s"circuit breaker: half-open -> open for $host") -> circuitBreaker.updated(host, State.Open(now))
        }.flatten *>
          circuitBreakersRef.get.map(_.toString()).flatMap(Common.logger.info)
      }

    }

  def create(): Task[PipelineStage] =
    Ref
      .make(Map.empty: CircuitBreakers)
      .map(stage(1, Duration(5, TimeUnit.SECONDS)))

}
