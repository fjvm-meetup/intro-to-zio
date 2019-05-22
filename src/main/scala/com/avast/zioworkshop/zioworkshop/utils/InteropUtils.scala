package com.avast.zioworkshop.zioworkshop.utils

import cats.effect.Resource
import scalaz.zio.interop.catz._
import scalaz.zio.{ Managed, Task }

object InteropUtils {
  def resourceToManaged[T](resource: Resource[Task, T]): Managed[Throwable, T] =
    Managed.make(resource.allocated)(_._2.orDie).map(_._1)
}
