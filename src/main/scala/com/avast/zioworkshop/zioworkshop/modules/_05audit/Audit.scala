package com.avast.zioworkshop.zioworkshop.modules._05audit

import com.avast.zioworkshop.zioworkshop.PipelineStage
import com.avast.zioworkshop.zioworkshop.db.audit.Postgres
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


  private def stage(postgres: Postgres): PipelineStage = PipelineStage.delegate

  def create(postgres: Postgres): Task[PipelineStage] = Task.succeed(stage(postgres))

}
