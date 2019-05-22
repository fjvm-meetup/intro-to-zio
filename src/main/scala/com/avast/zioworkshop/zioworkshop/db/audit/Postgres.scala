package com.avast.zioworkshop.zioworkshop.db.audit

import java.net.InetSocketAddress
import java.sql.{ DriverManager, Timestamp }
import java.time.Instant

import org.http4s.Uri

class Postgres(connUri: String, user: String, password: String) extends AutoCloseable {
  private val jdbcMgr = DriverManager.getConnection(connUri, user, password)

  private val init: String =
    """
      | create extension if not exists "uuid-ossp";
      |
      |
      | create table if not exists audit (
      |    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
      |    timestamp timestamp NOT NULL,  
      |    source varchar(50),  
      |    target varchar(500) NOT NULL
      | )
    """.stripMargin

  private val insert: String =
    """
      | insert into audit (timestamp, source, target) VALUES (?, ?, ?)
    """.stripMargin

  def initSchema(): Boolean =
    jdbcMgr.prepareStatement(init).execute()

  def audit(now: Instant, clientIp: Option[InetSocketAddress], target: Uri): Int = {
    val s = jdbcMgr.prepareStatement(insert)
    s.setTimestamp(1, Timestamp.from(now))
    s.setString(2, clientIp.map(_.getAddress.getHostAddress).orNull)
    s.setString(3, target.toString())
    s.executeUpdate()
  }

  override def close(): Unit =
    jdbcMgr.close()
}

object Postgres {
  def create(): Postgres =
    new Postgres("jdbc:postgresql://127.0.0.1:5432/proxydb", "proxy", "password")
}
