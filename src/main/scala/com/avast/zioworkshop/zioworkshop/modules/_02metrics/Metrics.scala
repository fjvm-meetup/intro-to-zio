package com.avast.zioworkshop.zioworkshop.modules._02metrics

import com.avast.zioworkshop.zioworkshop.PipelineStage
import scalaz.zio.Task

/**
 * Measure how long the response took. Extend HTTP response headers with it.
 * Hints:
 *   - Common.clock#nanoTime
 *   - org.http4s.Header#apply
 *   - org.http4s.Message#withHeaders
 */
object Metrics {

  private def stage: PipelineStage = PipelineStage.delegate

  def create(): Task[PipelineStage] = Task.succeed(stage)

}
