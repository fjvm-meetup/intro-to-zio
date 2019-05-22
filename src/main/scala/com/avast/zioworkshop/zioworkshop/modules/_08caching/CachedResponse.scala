package com.avast.zioworkshop.zioworkshop.modules._08caching

import cats.effect.Sync
import com.avast.zioworkshop.zioworkshop.HttpResponse
import fs2.Chunk
import org.http4s.Response
import scalaz.zio.Task

case class CachedResponse(response: HttpResponse, body: Vector[Byte]) {
  def toResponse: HttpResponse = {
    import scalaz.zio.interop.catz._
    encodeBody(response, body)
  }
  private def encodeBody[F[_]: Sync](resp: Response[F], body: Vector[Byte]): Response[F] =
    resp.copy(body = fs2.Stream.chunk(Chunk.array(body.toArray)))
}

object CachedResponse {

  def fromHttpResponse(httpResponse: HttpResponse): Task[CachedResponse] = {
    import scalaz.zio.interop.catz._
    decodeBody(httpResponse).map(b => CachedResponse(httpResponse, b))
  }

  private def decodeBody[F[_]: Sync](httpResponse: Response[F]): F[Vector[Byte]] =
    httpResponse.attemptAs[Array[Byte]].map(_.toVector).getOrElse(Vector.empty[Byte])
}
