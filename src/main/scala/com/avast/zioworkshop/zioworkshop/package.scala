package com.avast.zioworkshop

import org.http4s.{ Request, Response }
import scalaz.zio.Task

package object zioworkshop {
  type HttpRequest  = Request[Task]
  type HttpResponse = Response[Task]
}
