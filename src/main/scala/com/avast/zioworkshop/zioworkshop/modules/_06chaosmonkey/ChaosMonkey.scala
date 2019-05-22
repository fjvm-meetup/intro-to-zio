package com.avast.zioworkshop.zioworkshop.modules._06chaosmonkey

import com.avast.zioworkshop.zioworkshop.PipelineStage
import scalaz.zio.Task

/**
 * Based on probability, return random errors (HTTP response 5xx) instead of the upstream.
 *
 * Hints:
 *   - Common.random
 *   - com.avast.zioworkshop.zioworkshop.utils.HttpDsl
 */
object ChaosMonkey {

  private def stage(config: Config): PipelineStage = PipelineStage.delegate

  def create(config: Config): Task[PipelineStage] = Task.succeed(stage(config))

  case class Config(chanceOf500: Double, minDelayMs: Int, maxDelayMs: Int)
}
