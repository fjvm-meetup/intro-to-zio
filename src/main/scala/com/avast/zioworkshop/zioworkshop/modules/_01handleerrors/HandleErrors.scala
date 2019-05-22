package com.avast.zioworkshop.zioworkshop.modules._01handleerrors

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{HttpResponse, PipelineStage}
import scalaz.zio.Task

/*
 * Upstream pileline stages may fail with Exception. Recover from this using predefined
 * "BadGateway" HTTP response.
 */
object HandleErrors {
  import Common.HttpDsl._

  // this is how you construct response - not the import above - it's required
  private def badGateway(errMsg: String): Task[HttpResponse] = BadGateway(errMsg)  


  private def stage: PipelineStage = PipelineStage.delegate

  def create(): Task[PipelineStage] = Task.succeed(stage)

}
