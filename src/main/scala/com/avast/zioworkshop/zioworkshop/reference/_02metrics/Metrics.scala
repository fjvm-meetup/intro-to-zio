package com.avast.zioworkshop.zioworkshop.reference._02metrics

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{ HttpRequest, HttpResponse, Pipeline, PipelineStage }
import org.http4s.Header
import scalaz.zio.Task
import scalaz.zio.duration.Duration

/**
 * Measure how long the response took. Extend HTTP response headers with it.
 * Hints:
 *   - Common.clock#nanoTime
 *   - org.http4s.Header#apply
 *   - org.http4s.Message#withHeaders
 */
object Metrics {

  private def stage: PipelineStage = new PipelineStage {

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      for {
        backThen <- Common.clock.nanoTime
        resp     <- next.run(request)
        now      <- Common.clock.nanoTime
      } yield resp.withHeaders(Header("X-Upstream-Duration-ms", Duration.fromNanos(now - backThen).toMillis.toString))
  }

  def create(): Task[PipelineStage] = Task.succeed(stage)

}
