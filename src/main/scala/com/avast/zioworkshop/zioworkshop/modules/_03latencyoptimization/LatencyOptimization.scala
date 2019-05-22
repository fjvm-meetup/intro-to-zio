package com.avast.zioworkshop.zioworkshop.modules._03latencyoptimization

import com.avast.zioworkshop.zioworkshop.PipelineStage
import scalaz.zio.Task
import scalaz.zio.duration.Duration

/**
 * Sometimes requests take a long time to resolve, but often this is an anomaly.
 * In order to optimize latency, wait for configurable amount of time and then fire
 * second concurrent request. Return the first response that arrives.
 *
 * This is usually safe only for GET requests.
 *
 * Hints:
 *   - scalaz.zio.Task#race()
 *   - Common.clock#sleep
 */
object LatencyOptimization {

  private def stage(delay: Duration): PipelineStage = PipelineStage.delegate

  def create(delay: Duration): Task[PipelineStage] = Task.succeed(stage(delay))

}
