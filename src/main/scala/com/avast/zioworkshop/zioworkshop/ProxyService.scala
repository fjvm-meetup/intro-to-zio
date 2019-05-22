package com.avast.zioworkshop.zioworkshop

import java.util.concurrent.TimeUnit

import cats.effect.Resource
import com.avast.zioworkshop.zioworkshop.db.audit.Postgres
import com.avast.zioworkshop.zioworkshop.modules._00accesslog.AccessLog
import com.avast.zioworkshop.zioworkshop.modules._01handleerrors.HandleErrors
import com.avast.zioworkshop.zioworkshop.modules._01timeout.Timeout
import com.avast.zioworkshop.zioworkshop.modules._02metrics.Metrics
import com.avast.zioworkshop.zioworkshop.modules._03latencyoptimization.LatencyOptimization
import com.avast.zioworkshop.zioworkshop.modules._04retry.Retry
import com.avast.zioworkshop.zioworkshop.modules._05audit.Audit
import com.avast.zioworkshop.zioworkshop.modules._06chaosmonkey.ChaosMonkey
import com.avast.zioworkshop.zioworkshop.modules._07throttling.Throttling
import com.avast.zioworkshop.zioworkshop.modules._08caching.Caching
import com.avast.zioworkshop.zioworkshop.modules._09circuitbreaker.CircuitBreaker
import com.avast.zioworkshop.zioworkshop.utils.Common
import com.avast.zioworkshop.zioworkshop.utils.Common.HttpDsl._
import org.http4s.HttpApp
import org.http4s.client.Client
import scalaz.zio.Task
import scalaz.zio.duration.Duration

class ProxyService(postgres: Postgres, upstreamChain: Pipeline) {

  private val ThrottlingLimit   = 5
  private val ChaosMonkeyConfig = ChaosMonkey.Config(0.3, 1000, 3000)

  def app: Task[HttpApp[Task]] =
    createPipeline.map(_.toPipeline(upstreamChain)).map { pipeline =>
      Client[Task] { request =>
        Resource.liftF(pipeline.run(request))
      }.toHttpApp
    }

  private def sandbox(t: Task[HttpResponse]): Task[HttpResponse] =
    t.sandbox.catchAll { err =>
      val errorMsg = s"fatal server error - probably a defect(s) ${err.map(_.getMessage)}"
      Common.logger.error(errorMsg, err.defects.head) *>
        InternalServerError(errorMsg)
    }

  private val createPipeline: Task[PipelineStage] = for {
    metrics             <- Metrics.create()
    accesslog           <- AccessLog.create()
    chaosMonkey         <- ChaosMonkey.create(ChaosMonkeyConfig)
    throttling          <- Throttling.create(ThrottlingLimit)
    latencyOptimization <- LatencyOptimization.create(Duration(50, TimeUnit.MILLISECONDS))
    timeouter           <- Timeout.create(Duration(3, TimeUnit.SECONDS))
    circuitBreaker      <- CircuitBreaker.create()
    caching             <- Caching.create()
    retry               <- Retry.create()
    handleErrors        <- HandleErrors.create()
    audit               <- Audit.create(postgres)
  } yield {

    metrics
      .andThen(accesslog)
      .andThen(handleErrors)
      .andThen(audit)
      .andThen(chaosMonkey)
      .andThen(throttling)
      .andThen(latencyOptimization)
      .andThen(timeouter)
      .andThen(circuitBreaker)
      .andThen(caching)
      .andThen(retry)
  }

}
