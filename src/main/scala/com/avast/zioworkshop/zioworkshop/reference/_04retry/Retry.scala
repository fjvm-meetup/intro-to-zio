package com.avast.zioworkshop.zioworkshop.reference._04retry

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{HttpRequest, HttpResponse, Pipeline, PipelineStage}
import scalaz.zio.duration._
import scalaz.zio.{Schedule, Task, UIO, ZSchedule}

/**
 * Retry failed requests. Experiment with different strategies.
 * Maybe some back-off would make sense? What even constitutes a failure?
 * Is it 500, is it 400? Is it SocketException? Up to you!
 *
 * Hints:
 *   - scalaz.zio.Task#retry
 *   - scalaz.zio.Schedule
 *     - take a look at the api and docs and experiment a little try to figure out a strategy that tries 5 times with
 *         exponential back-off
 */
object Retry {

  private def stage: PipelineStage = new PipelineStage {

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] = {
      val autofail = next.run(request).flatMap { resp =>
        if (resp.status.code < 500) {
          UIO.succeed(resp)
        } else {
          fail(request, resp)
        }
      }

      autofail.retry(schedule).provide(Common)
    }
  }

  private val schedule: ZSchedule[Any, Throwable, (Duration, Int)] =
    Schedule.exponential(1.second) && Schedule.recurs(10)

  private def fail(req: HttpRequest, resp: HttpResponse): Task[Nothing] = {
    val msg = s"request: $req failed with response: $resp"
    Common.logger.info(msg) *> Task.fail(new Exception(msg))
  }

  def create(): Task[PipelineStage] = Task.succeed(stage)

}
