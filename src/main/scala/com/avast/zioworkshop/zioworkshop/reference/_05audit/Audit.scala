package com.avast.zioworkshop.zioworkshop.reference._05audit

import java.time.Instant
import java.util.concurrent.TimeUnit

import com.avast.zioworkshop.zioworkshop.db.audit.Postgres
import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{ HttpRequest, HttpResponse, Pipeline, PipelineStage }
import scalaz.zio.Task

/**
 * Store information about each upstream call and it's response into database.
 *
 * You can use [[Postgres]] implementation to do the jdbc calls - just make sure you handle all the db errors!
 *
 * Also, be warned - jdbc is a blocking API and will block in the ZIO threadpool that is designed for async calls.
 * Hint:
 *  - scalaz.zio.blocking
 *  - scalaz.zio.Task#catchAll
 * - Common.clock.currentTime
 */
object Audit {

  private def nowTask: Task[Instant] = Common.clock.currentTime(TimeUnit.MILLISECONDS).map(Instant.ofEpochMilli)

  private def stage(postgres: Postgres): PipelineStage = new PipelineStage {

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      for {
        resp <- next.run(request)
        now  <- nowTask
        _ <- scalaz.zio.blocking
              .effectBlocking(postgres.audit(now, request.remote, request.uri))
              .provide(Common)
              .catchAll(thr => Common.logger.warn("error when storing audit info", thr))

      } yield resp
  }

  def create(postgres: Postgres): Task[PipelineStage] = Task.succeed(stage(postgres))

}
