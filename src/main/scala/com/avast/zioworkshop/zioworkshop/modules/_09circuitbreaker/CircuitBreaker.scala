package com.avast.zioworkshop.zioworkshop.modules._09circuitbreaker

import com.avast.zioworkshop.zioworkshop.PipelineStage
import scalaz.zio.Task

/**
 * See for general idea: https://en.wikipedia.org/wiki/Circuit_breaker_design_pattern
 * This is NOT 3 minutes job. But you are welcome to try.
 * Hints:
 *    - scalaz.zio.Ref
 *    - Common.clock
 */
object CircuitBreaker {
  private def stage(): PipelineStage = PipelineStage.delegate

  def create(): Task[PipelineStage] = Task.succeed(stage())

}
