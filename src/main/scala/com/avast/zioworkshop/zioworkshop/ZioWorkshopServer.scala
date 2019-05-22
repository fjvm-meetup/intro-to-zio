package com.avast.zioworkshop.zioworkshop

import java.net.InetSocketAddress

import com.avast.zioworkshop.zioworkshop.db.audit.Postgres
import com.avast.zioworkshop.zioworkshop.utils.InteropUtils._
import com.avast.zioworkshop.zioworkshop.utils.UpstreamClient
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.interop.catz.implicits._

import scala.concurrent.ExecutionContext.global

object ZioWorkshopServer {

  def apply[R]()(implicit runtime: Runtime[R]): Managed[Throwable, Server[Task]] =
    for {
      client         <- resourceToManaged(BlazeClientBuilder[Task](global).resource)
      upstreamClient <- Managed.fromEffect(UpstreamClient.create(client))
      postgres       <- Managed.make(acquirePostgres)(releasePostgres)
      httpApp        <- Managed.fromEffect(new ProxyService(postgres, upstreamClient).app)
      server <- resourceToManaged(
                 BlazeServerBuilder[Task]
                   .bindSocketAddress(InetSocketAddress.createUnresolved("0.0.0.0", 3333))
                   .withHttpApp(httpApp)
                   .resource
               )
    } yield server

  private def acquirePostgres: Task[Postgres] =
    for {
      dao <- Task.effect(Postgres.create())
      _   <- Task.effect(dao.initSchema())
    } yield dao

  private def releasePostgres(p: Postgres): UIO[Unit] = ZIO.effect(p.close()).catchAll { thr =>
    ZIO.effectTotal(println(s"error when closing postgres: $thr"))
  }

}
