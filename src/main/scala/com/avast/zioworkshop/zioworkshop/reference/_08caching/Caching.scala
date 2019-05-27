package com.avast.zioworkshop.zioworkshop.reference._08caching

import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.{HttpRequest, HttpResponse, Pipeline, PipelineStage}
import org.http4s.{Method, Uri}
import scalaz.zio.{Ref, Task}

/**
 * Cache All Get responses into a map and retrieve those from cache ... match on path, ignore headers etc
 *  Probably cache only successful response.
 *  Hints:
 *   - scalaz.zio.Ref
 */
object Caching {

  private def stage(ref: Ref[Map[Uri, CachedResponse]]): PipelineStage = new PipelineStage {

    /**
     * Process the provided request and when done, send it up the chain by calling `chain` which provides access point to rest of the pipeline
     *
     * @param next    rest of the pipeline
     * @param request current request
     */
    override def run(next: Pipeline)(request: HttpRequest): Task[HttpResponse] =
      if (request.method == Method.GET) {
        attemptFromCache(next, request)
      } else {
        next.run(request)
      }

    private def attemptFromCache(next: Pipeline, request: HttpRequest) =
      ref.get.flatMap(
        _.get(request.uri)
          .map(logAndReturnResp(request))
          .getOrElse(runAndUpdateRef(next, request))
      )

    private def logAndReturnResp(request: HttpRequest)(resp: CachedResponse) =
      Common.logger.info(
        s"content for uri: ${request.uri} found in cache"
      ) *> Task.succeed(resp.toResponse)

    private def runAndUpdateRef(next: Pipeline, request: HttpRequest) =
      for {
        resp       <- next.run(request)
        cachedResp <- CachedResponse.fromHttpResponse(resp)
        _          <- ref.update(_.updated(request.uri, cachedResp))
      } yield {
        resp
      }
  }

  def create(): Task[PipelineStage] = Ref.make(Map.empty[Uri, CachedResponse]).map(stage)

}
