package com.avast.zioworkshop.zioworkshop.reference._08caching

import cats.effect.Sync
import com.avast.zioworkshop.zioworkshop.HttpResponse
import org.http4s.Response
import scalaz.zio.Task

case class CachedResponse(response: HttpResponse, body: Array[Byte]) {
  def toResponse: HttpResponse = {
    import scalaz.zio.interop.catz._
    encodeBody(response, body)
  }
  private def encodeBody[F[_]: Sync](resp: Response[F], body: Array[Byte]): Response[F] =
    resp.copy(body = fs2.Stream.emits(body))
}

object CachedResponse {

  def fromHttpResponse(httpResponse: HttpResponse): Task[CachedResponse] = {
    import scalaz.zio.interop.catz._
    decodeBody(httpResponse).map(b => CachedResponse(httpResponse, b))
  }

  private def decodeBody[F[_]: Sync](httpResponse: Response[F]): F[Array[Byte]] =
    httpResponse.attemptAs[Array[Byte]].getOrElse(Array.emptyByteArray)
}
