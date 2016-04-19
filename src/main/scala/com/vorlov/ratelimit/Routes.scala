package com.vorlov.ratelimit

import com.vorlov.ratelimit.model.RateLimitRule
import com.vorlov.ratelimit.service.{RateLimitPolicyService, RateLimitService}
import com.vorlov.ratelimit.service.impl.{H2RateLimitPolicyService, RedisRateLimitService}
import org.http4s.{Status, Response, HttpService}
import org.http4s.dsl._
import org.slf4j.LoggerFactory

import scalaz.concurrent.Task

import com.vorlov.ratelimit.utils.StringHelpers._

/**
  * Service's public RESTful API
  */
class Routes(rateLimitService: RateLimitService, rateLimitPolicyService: RateLimitPolicyService) {

  private val log = LoggerFactory.getLogger(classOf[Routes])

  private def errorHandler: PartialFunction[Throwable, Task[Response]] = {
    case t: Throwable =>
      log.error("Could not handle request", t)
      InternalServerError("We could not handle your request, please com back later")
  }

  val service = HttpService {

    case req @ PUT -> Root / "policy" / userID / requests / "per" / period  =>
      if(!period.isAllDigits) BadRequest("Period should be a number.")
      else if(!requests.isAllDigits) BadRequest("Period should be a number.")
      else {

        req.decode[String] {
          description =>

            Ok(rateLimitPolicyService.add(userID, RateLimitRule(description, period.toLong, requests.toLong)).map {
              _ => ""
            }).handleWith(errorHandler)
        }

      }

    case DELETE -> Root / "policy" / userID / requests / "per" / period  =>
      if(!period.isAllDigits) BadRequest("Period should be a number.")
      else if(!requests.isAllDigits) BadRequest("Period should be a number.")
      else {
        Ok(rateLimitPolicyService.remove(userID, RateLimitRule("", period.toLong, requests.toLong)).map {
          _ => ""
        }).handleWith(errorHandler)
      }

    case GET -> Root / "policy" / userID  =>
      Ok(rateLimitPolicyService.allByUserID(userID).map(_.mkString("\n"))).handleWith(errorHandler)

    case PUT -> Root / userID =>
      Ok(rateLimitService.report(userID).map(_ => "")).handleWith(errorHandler)

    case GET -> Root / userID =>

      for{
        policies <- rateLimitPolicyService.allByUserID(userID)
        errors <- rateLimitService.verify(userID, policies)
      } yield {
        if(errors.isEmpty) Response(status = Status.Ok)
        else Response(status = Status.Conflict)
      }

  }

}
