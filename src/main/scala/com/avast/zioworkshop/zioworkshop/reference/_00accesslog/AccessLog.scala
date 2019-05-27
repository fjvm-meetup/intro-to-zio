package com.avast.zioworkshop.zioworkshop.reference._00accesslog

import java.time.Instant
import java.util.concurrent.TimeUnit

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{HttpRequest, HttpResponse, Pipeline, PipelineStage}
import scalaz.zio.{Task, UIO}

/*
 * This is an example module. You can use it as inspiration.
 */
object AccessLog {

  def create(): Task[PipelineStage] = Task.succeed(stage) // factory method for accesslog stage

  private def stage: PipelineStage = new PipelineStage {
    override def run(pipeline: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      pipeline
        .run(request) // send request up the stream and obtain Task with response
        .foldM(
          err => handleFailure(request, err),   // handle failure
          resp => handleResponse(request, resp) // handle success
        )
  }

  // task that returns current time, whenever evaluated
  private val getTime = Common.clock.currentTime(TimeUnit.MILLISECONDS).map(Instant.ofEpochMilli)

  private def handleFailure(request: HttpRequest, throwable: Throwable): Task[Nothing] = {
    val writeToConsole = for {
      currentTime <- getTime
      _           <- Common.console.putStrLn(formatErr(currentTime, request, throwable))
    } yield ()

    writeToConsole.flatMap(_ => Task.fail(throwable)) // fail again so that error is not lost
  }

  private def handleResponse(request: HttpRequest, response: HttpResponse): UIO[HttpResponse] =
    for {
      now <- getTime
      _   <- Common.console.putStrLn(formatSuccess(now, request, response))
    } yield response

  private def formatErr(now: Instant, request: HttpRequest, throwable: Throwable): String = {
    val errMsg = Option(throwable.getMessage).getOrElse(throwable.getClass.getCanonicalName)
    s"$now ${request.method} ${request.uri} ERR $errMsg"
  }
  private def formatSuccess(now: Instant, request: HttpRequest, response: HttpResponse): String =
    s"$now ${request.method} ${request.uri}: ${response.status}"


}
