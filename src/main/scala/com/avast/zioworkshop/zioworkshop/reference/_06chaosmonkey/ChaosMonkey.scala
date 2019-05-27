package com.avast.zioworkshop.zioworkshop.reference._06chaosmonkey

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{HttpRequest, HttpResponse, Pipeline, PipelineStage}
import scalaz.zio.Task
import scalaz.zio.duration.Duration

/**
 * Based on probability, return random errors (HTTP response 5xx) instead of the upstream.
 *
 * Hints:
 *   - Common.random
 *   - com.avast.zioworkshop.zioworkshop.utils.HttpDsl
 */
object ChaosMonkey {

  private def stage(config: Config): PipelineStage = new PipelineStage {
    import Common.HttpDsl._

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      delay(config.minDelayMs, config.maxDelayMs) *> randomError(next.run(request))

    private def delay(min: Int, max: Int) =
      Common.random
        .nextInt(max - min)
        .map(_ + min)
        .flatMap(ms => Common.clock.sleep(Duration.fromNanos(ms * 1000 * 1000)))

    private def randomError(actualResponse: Task[HttpResponse]): Task[HttpResponse] =
      Common.random.nextDouble >>= { rand =>
        if (rand < config.chanceOf500) {
          InternalServerError("Chaos monkey goin wild!")
        } else {
          actualResponse
        }
      }
  }

  def create(config: Config): Task[PipelineStage] = Task.succeed(stage(config))

  case class Config(chanceOf500: Double, minDelayMs: Int, maxDelayMs: Int)
}
