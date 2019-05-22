package com.avast.zioworkshop.zioworkshop.utils

import cats.Monad
import cats.effect.Bracket
import org.http4s.dsl.Http4sDsl
import org.slf4j.LoggerFactory
import scalaz.zio.blocking.Blocking
import scalaz.zio.clock.Clock
import scalaz.zio.console.Console
import scalaz.zio.interop.catz._
import scalaz.zio.random.Random
import scalaz.zio.system.System
import scalaz.zio.{Task, UIO}
object Common extends Clock.Live with System.Live with Console.Live with Random.Live with Blocking.Live {

  object HttpDsl extends Http4sDsl[Task] {
    implicit val monadTask = implicitly[Monad[Task]]
    implicit val bracket   = implicitly[Bracket[Task, Throwable]]
  }


  object logger {
    private val logger = LoggerFactory.getLogger("ZIO-workshop-logger")
    def trace(msg: String): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isTraceEnabled) {
          logger.trace(msg)
        }
      }
    def debug(msg: String): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isDebugEnabled) {
          logger.debug(msg)
        }
      }
    def debug(msg: String, throwable: Throwable): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isDebugEnabled) {
          logger.debug(msg, throwable)
        }
      }
    def info(msg: String): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isInfoEnabled) {
          logger.info(msg)
        }
      }
    def info(msg: String, throwable: Throwable): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isInfoEnabled) {
          logger.info(msg, throwable)
        }
      }
    def warn(msg: String): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isWarnEnabled) {
          logger.warn(msg)
        }
      }
    def warn(msg: String, throwable: Throwable): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isWarnEnabled) {
          logger.warn(msg, throwable)
        }
      }
    def error(msg: String, throwable: Throwable): UIO[Unit] =
      UIO.effectTotal {
        if (logger.isErrorEnabled) {
          logger.error(msg, throwable)
        }
      }
  }
}
