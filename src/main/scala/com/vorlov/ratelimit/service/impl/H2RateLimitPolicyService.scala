package com.vorlov.ratelimit.service.impl

import com.vorlov.ratelimit.model.RateLimitRule
import com.vorlov.ratelimit.model.schema.RateLimitPolicySchema
import com.vorlov.ratelimit.service.RateLimitPolicyService
import org.slf4j.LoggerFactory

import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import com.vorlov.ratelimit.utils.ScalazHelpers._
import scalaz.concurrent.Task

/**
  * An implementation of the [[RateLimitPolicyService]] interface that keeps policies in H2 database
  */
class H2RateLimitPolicyService extends RateLimitPolicyService{

  private val log = LoggerFactory.getLogger(classOf[H2RateLimitPolicyService])

  private val db = Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  private val policies = TableQuery[RateLimitPolicySchema]

  db.run(policies.schema.create).collect {
    case _ =>
  } recover {
    case t: Throwable => log.error("Could not create policies schema", t)
  }

  /**
    *
    * @inheritdoc
    */
  override def allByUserID(userID: String): Task[Seq[RateLimitRule]] = {

    val q = for{
      p <- policies if p.userKey === userID
    } yield {
      (p.description, p.duration, p.maxRequests)
    }

    db.run(q.result).map {
      result =>
          result.map {
            row => RateLimitRule(row._1, row._2, row._3)
          }
    }
  }

  /**
    *
    * @inheritdoc
    */
  override def remove(userID: String, policy: RateLimitRule): Task[Unit] = {
    val q = policies.filter(p => p.userKey === userID && p.maxRequests === policy.maxRequests && p.duration === policy.duration)
    db.run(q.delete).map {
      result =>
    }
  }

  /**
    *
    * @inheritdoc
    */
  override def add(userID: String, policy: RateLimitRule): Task[Unit] = {
    db.run (
      policies += (policy.description, userID, policy.duration, policy.maxRequests)
    ).map{
      result =>
    }
  }

}
