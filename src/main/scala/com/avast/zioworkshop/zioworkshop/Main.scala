package com.avast.zioworkshop.zioworkshop

import com.avast.zioworkshop.zioworkshop.utils.Common._
import scalaz.zio.{App, DefaultRuntime, ZIO}

object Main extends App {
  implicit val runtime: DefaultRuntime = this

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    ZioWorkshopServer()
      .use(_ => ZIO.never)
      .foldM(
        throwable => logger.error("Fatal server failure", throwable) *> ZIO.succeed(1),
        _ => ZIO.succeed(0)
      )

}
