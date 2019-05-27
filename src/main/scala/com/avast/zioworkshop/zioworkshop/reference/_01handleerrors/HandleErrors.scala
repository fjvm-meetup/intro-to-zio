package com.avast.zioworkshop.zioworkshop.reference._01handleerrors

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{HttpRequest, HttpResponse, Pipeline, PipelineStage}
import scalaz.zio.Task

/*
 * Upstream pileline stages may fail with Exception. Recover from this using predefined
 * "BadGateway" HTTP response.
 */
object HandleErrors {
  import Common.HttpDsl._

  // this is how you construct response - not the import above - it's required
  private def badGateway(errMsg: String): Task[HttpResponse] = BadGateway(errMsg)  


  private def stage: PipelineStage = new PipelineStage {

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      next.run(request).catchAll(err => badGateway(err.getMessage))
  }

  def create(): Task[PipelineStage] = Task.succeed(stage)

}
