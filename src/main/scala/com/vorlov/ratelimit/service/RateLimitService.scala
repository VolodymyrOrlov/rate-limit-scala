package com.vorlov.ratelimit.service

import com.vorlov.ratelimit.model.RateLimitRule

import scalaz.concurrent.Task

/**
  * An interface for the rate limit service
  */
trait RateLimitService {

  /**
    * Report a request made by a given user
    *
    * @param userID user's key
    * @return ScalaZ's [[Task]]
    */
  def report(userID: String): Task[Unit]

  /**
    * Verify whether given user has reached his/her limits
 *
    * @param userID user's key
    * @param policy a set of rules which service will use to verify whether limit has been reached
    * @return empty if no limits have been reached. List of exceeded limits otherwise
    */
  def verify(userID: String, policy: Seq[RateLimitRule]): Task[Seq[String]]

}
