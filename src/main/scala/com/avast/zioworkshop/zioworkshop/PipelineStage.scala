package com.avast.zioworkshop.zioworkshop

import scalaz.zio.Task

trait PipelineStage { self =>

  /**
   * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
   * @param next rest of the pipeline
   * @param request current request
   */
  def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse]

  /**
   * Compile this stage into [[Pipeline]] by providing rest of the pipeline
   *
   * @param next rest of the pipeline
   */
  def toPipeline(next: Pipeline): Pipeline = new Pipeline {
    override def run(req: HttpRequest): Task[HttpResponse] = self.run(next)(req)
  }

  /**
   * Merge this pipeline with [[next]] pipeline. This will result in [[Pipeline]] that first processes request according to [[Pipeline.run]] of this stage and uses provided PipelineStage as upstream of this PipelineStage
   *
   * @param next rest of the pipeline
   */
  def andThen(next: PipelineStage): PipelineStage = new PipelineStage {
    override def run(chain: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      self.run(next.toPipeline(chain))(request)
  }

}

object PipelineStage {
  def apply(f: (Pipeline, HttpRequest) => Task[HttpResponse]): PipelineStage = new PipelineStage {
    override def run(chain: Pipeline)(request: HttpRequest): Task[HttpResponse] = f(chain, request)
  }

  /**
   * Dummy implementation that only calls `chain` pipeline
   * @return
   */
  @deprecated("Dummy implementation - provide one youself", "")
  val delegate: PipelineStage = new PipelineStage {
    override def run(chain: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      chain.run(request)
  }

  /**
   * Pipeline stage that doesn't forward request 'upstream'
   * @return
   */
  def chainless(f: HttpRequest => Task[HttpResponse]): PipelineStage = new PipelineStage {
    override def run(chain: Pipeline)(request: HttpRequest): Task[HttpResponse] = f(request)
  }
}
