package com.avast.zioworkshop.zioworkshop.modules._08caching

import com.avast.zioworkshop.zioworkshop.PipelineStage
import scalaz.zio.Task

/**
 * Cache All Get responses into a map and retrieve those from cache ... match on path, ignore headers etc
 *  Probably cache only successful response.
 *  Hints:
 *   - scalaz.zio.Ref
 *   - CachedResponse.fromResponse and .toResponse - use this as object that is actually being cached - this solves
 *       some http4s issues
 */
object Caching {

  private def stage(): PipelineStage = PipelineStage.delegate

  def create(): Task[PipelineStage] = Task.succeed(stage())

}
