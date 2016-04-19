package com.vorlov.ratelimit.model.schema

import slick.driver.H2Driver.api._

/**
  * An ORM helper
  * @param tag
  */
class RateLimitPolicySchema(tag: Tag) extends Table[(String, String, Long, Long)](tag, "policy") {

  def userKey = column[String]("USER_KEY")

  def description = column[String]("DESCRIPTION")

  def duration = column[Long]("DURATION")

  def maxRequests = column[Long]("MAX_REQUESTS")

  def pk = primaryKey("POLICY_ID", (userKey, duration, maxRequests))

  def * = (description, userKey, duration, maxRequests)

}