package com.avast.zioworkshop.zioworkshop.modules._01timeout

import com.avast.zioworkshop.zioworkshop._
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
  private def stage(duration: Duration): PipelineStage =  PipelineStage.delegate

  def create(duration: Duration): Task[PipelineStage] = Task.succeed(stage(duration))

}
