package com.vorlov.ratelimit

import com.vorlov.ratelimit.model.RateLimitRule
import com.vorlov.ratelimit.service.{RateLimitService, RateLimitPolicyService}
import org.http4s.{Status, Uri, Method, Request}
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable
import scalaz.concurrent.Task

class RoutesSpec extends WordSpec with Matchers {

  class MockRateLimitPolicyService extends RateLimitPolicyService {

    val rules =  mutable.Buffer.empty[(String, RateLimitRule)]

    override def allByUserID(userID: String): Task[Seq[RateLimitRule]] = Task.now(rules.map(_._2))

    override def remove(userID: String, policy: RateLimitRule): Task[Unit] = Task.now{
      rules.indexWhere(v => v._2.duration == policy.duration && v._2.maxRequests == policy.maxRequests && v._1 == userID) match {
        case i if i >= 0 => rules.remove(i)
        case _ =>
      }
    }

    override def add(userID: String, policy: RateLimitRule): Task[Unit] = Task.now{
      rules += ((userID, policy))
    }
  }

  class MockRateLimitService extends RateLimitService {

    val counters = mutable.Map.empty[String, Int].withDefaultValue(0)

    override def report(userID: String): Task[Unit] = Task.now {
      counters(userID) = counters(userID) + 1
    }

    override def verify(userID: String, policy: Seq[RateLimitRule]): Task[Seq[String]] = Task.now {
      counters.get(userID).map {
        case c if policy.exists(p => p.maxRequests >= c) => Seq("limit exceeded")
        case c => Seq.empty[String]
      } getOrElse(Seq.empty[String])

    }

  }

  trait Service {
    val rateLimitService = new MockRateLimitService
    val rateLimitPolicyService = new MockRateLimitPolicyService
    val service = new Routes(rateLimitService, rateLimitPolicyService).service
  }

  "Routes" should {

    "correctly add and retrieve rules" in new Service {

      val response = service.run(new Request(method = Method.PUT, uri = Uri(path = "/policy/userA/10/per/60"))).run

      rateLimitPolicyService.rules.head should ===(("userA", RateLimitRule("", 60, 10)))

    }

    "correctly remove limit rules" in new Service {

      service.run(new Request(method = Method.PUT, uri = Uri(path = "/policy/userA/10/per/60"))).run

      rateLimitPolicyService.rules.head should ===(("userA", RateLimitRule("", 60, 10)))

      service.run(new Request(method = Method.DELETE, uri = Uri(path = "/policy/userA/10/per/60"))).run

      rateLimitPolicyService.rules.headOption should ===(None)

    }

    "correctly report request" in new Service {

      rateLimitService.counters should not contain ("userA" -> 1)

      val response = service.run(new Request(method = Method.PUT, uri = Uri(path = "/userA"))).run

      rateLimitService.counters should contain ("userA" -> 1)

    }

    "identify reached limits" in new Service {

      service.run(new Request(method = Method.PUT, uri = Uri(path = "/policy/userA/1/per/60"))).run

      service.run(new Request(method = Method.PUT, uri = Uri(path = "/userA"))).run

      val response = service.run(new Request(method = Method.GET, uri = Uri(path = "/userA"))).run

      response.status should === (Status.Conflict)

    }

  }

}
