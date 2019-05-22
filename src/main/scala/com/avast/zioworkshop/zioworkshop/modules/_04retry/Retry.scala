package com.avast.zioworkshop.zioworkshop.modules._04retry

import com.avast.zioworkshop.zioworkshop.PipelineStage
import scalaz.zio.Task

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

  private def stage: PipelineStage = PipelineStage.delegate


  def create(): Task[PipelineStage] = Task.succeed(stage)

}
