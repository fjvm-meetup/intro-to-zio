package com.avast.zioworkshop.zioworkshop.reference._01timeout

import com.avast.zioworkshop.zioworkshop._
import com.avast.zioworkshop.zioworkshop.utils.Common
import scalaz.zio.Task
import scalaz.zio.duration.Duration

/**
 * We don't want to wait for ever. Time out after configurable amount of seconds.
 * Return Gateway Timeout HTTP response in case of timeout.
 *
 * Hints:
 * - there is a useful method on Task that you can use
 * - or you can implement it yourself using primitives
 *   - Task.race
 *   - Common.clock.sleep
 */
object Timeout {
  import Common.HttpDsl._
  private def stage(duration: Duration): PipelineStage = new PipelineStage {

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      next
        .run(request)
        .timeout(duration)
        .provide(Common)
        .flatMap(_.map(Task.succeed).getOrElse(err))
    
    private val err = GatewayTimeout(s"Request timeouted after $duration")
  }

  def create(duration: Duration): Task[PipelineStage] = Task.succeed(stage(duration))

}
