package com.avast.zioworkshop.zioworkshop.modules._07throttling

import com.avast.zioworkshop.zioworkshop.PipelineStage
import scalaz.zio.Task

/**
 * Limit the number of upstream connection that we do at one time.
 * Throttle can implemented per client, per upstream host or both.
 *
 * There are generally two types of throttling (a.k.a rate limiting):
 *  - limit number of concurrent requests (easier to implement)
 *  - limit number of requests per second (harder! maybe try this one at home :) )
 *
 *  Hints:
 *   - scalaz.zio.Ref
 *   - you might need to modify `stage(limit: Int)` signature
 *   - Something like this could by useful if you decide limit per second:
 *    Common.console
 *          .putStr("hello")
 *          .repeat(ZSchedule.fixed(Duration(1, TimeUnit.SECONDS)))
 *          .provide(Common)
 *
 */
object Throttling {

  private def stage(limit: Int): PipelineStage = PipelineStage.delegate

  def create(limit: Int): Task[PipelineStage] = Task.succeed(stage(limit))

}
