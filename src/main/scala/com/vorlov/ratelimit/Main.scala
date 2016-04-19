package com.vorlov.ratelimit

import java.util.concurrent.Executors

import com.vorlov.ratelimit.service.impl.{H2RateLimitPolicyService, RedisRateLimitService}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{ServerBuilder, Server, ServerApp}

import scala.util.Properties._
import scalaz.concurrent.Task

/**
  * Program's entry point. It sets up and launches HTTP service listening to port 7575
  */
object Main extends ServerApp{

  private val pool = Executors.newCachedThreadPool()

  private val ip =   "0.0.0.0"
  private val port = envOrNone("HTTP_PORT") map(_.toInt) getOrElse(7575)

  // for a real service I'd rather use ScalaZ's Reader to inject dependencies here.
  val routes = new Routes(new RedisRateLimitService, new H2RateLimitPolicyService)

  def build(): ServerBuilder ={
    BlazeBuilder
      .bindHttp(port, ip)
      .mountService(routes.service)
      .withServiceExecutor(pool)

  }

  def server(args: List[String]): Task[Server] = build().start

}

