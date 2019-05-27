package com.avast.zioworkshop.zioworkshop.reference._07throttling

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{ HttpRequest, HttpResponse, Pipeline, PipelineStage }
import org.http4s.Response
import scalaz.zio.{ Ref, Task, UIO }

/**
 * Limit the number of upstream connection that we do at one time.
 * Throttle can implemented per client, per upstream host or both.
 *
 * There are generally two types of throttling (a.k.a rate limiting):
 *  - limit number of concurrent requests (easier to implement)
 *  - limit number of requests per second (harder!)
 *
 *  Hints:
 *   - scalaz.zio.Ref
 *  - Something like this could by useful if you decide limit per second:
 *    Common.console
 *          .putStr("hello")
 *          .repeat(ZSchedule.fixed(Duration(1, TimeUnit.SECONDS)))
 *          .provide(Common)
 *
 */
object Throttling {

  private def stage(limit: Int, ref: Ref[Int]): PipelineStage = new PipelineStage {

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      for {
        goOrNo <- getTicket
        resp <- if (goOrNo) {
                 go(next, request)
               } else {
                 nogoResponse
               }
      } yield {
        resp
      }

    private def getTicket: UIO[Boolean] = ref.modify[Boolean] {
      case x if x < limit => (true, x + 1)
      case x              => (false, x)
    }

    private def go(next: Pipeline, request: HttpRequest): Task[HttpResponse] =
      next.run(request) <* ref.update(_ - 1)

    private def nogoResponse: UIO[HttpResponse] = UIO.succeed(Response(Common.HttpDsl.TooManyRequests))
  }

  def create(limit: Int): Task[PipelineStage] =
    Ref.make(0).map(stage(limit, _))

}
