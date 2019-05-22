package com.avast.zioworkshop.zioworkshop.utils

import com.avast.zioworkshop.zioworkshop.Pipeline
import org.http4s.client.Client
import scalaz.zio.interop.catz._
import scalaz.zio.{ Task, UIO }

/*
 * TODO interrupt safety
 */
object UpstreamClient {

  def create(client: Client[Task]): UIO[Pipeline] =
    UIO.succeed {
      Pipeline { request =>
        /**
         * Following implementation is not safe as it contains possible resource leak.
         * The leak occurs when response body is never consumed. Please do not use it on
         * anything serious. Proper solution would require to use [[scalaz.zio.Managed]]
         * which is out of scope of this hands-on session.
         * You have been warned.
         */
        client
          .run(request)
          .allocated
          .map {
            case (response, close) =>
              response.copy[Task](
                body = fs2.Stream
                  .bracket[Task, fs2.Stream[Task, Byte]](Task.succeed(response.body))(_ => close)
                  .flatten
              )
          }
      }
    }
}
