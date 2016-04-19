package com.vorlov.ratelimit.service

import com.vorlov.ratelimit.model.RateLimitRule

import scalaz.concurrent.Task

/**
  * A service that is used to add/delete/retrieve rate limit rules.
  */
trait RateLimitPolicyService {

  /**
    * Retrieve all rules for a given user
    * @param userID user's key
    * @return a list of rules as [[RateLimitRule]]
    */
  def allByUserID(userID: String): Task[Seq[RateLimitRule]]

  /**
    * Add a rule for a user
    * @param userID user's key
    * @param policy a rule
    * @return ScalaZ's [[Task]]
    */
  def add(userID: String, policy: RateLimitRule): Task[Unit]

  /**
    * Remove given rule for a user
    * @param userID user's key
    * @param policy a rule
    * @return ScalaZ's [[Task]]
    */
  def remove(userID: String, policy: RateLimitRule): Task[Unit]

}
