package com.avast.zioworkshop.zioworkshop

import scalaz.zio.Task

/**
 * Representation of compilation of multiple [[PipelineStage]]s that is ready to be ran
 */
trait Pipeline {

  /**
   * Process pipeline for given request
   */
  def run(req: HttpRequest): Task[HttpResponse]
}

object Pipeline {
  def apply(f: HttpRequest => Task[HttpResponse]): Pipeline = new Pipeline {
    override def run(req: HttpRequest): Task[HttpResponse] =
      f(req)
  }
}
