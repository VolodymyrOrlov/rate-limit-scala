package com.vorlov.ratelimit.model

/**
  * A model that represents a rate limit rule.
  *
  * @param description rule's description
  * @param duration    rule's duration in seconds
  * @param maxRequests rule's maximum number of requests for a given period
  */
case class RateLimitRule(description: String, duration: Long, maxRequests: Long) {

  override def toString(): String = {
    s"${if (description.isEmpty) "no description" else description}: [$maxRequests] requests per [$duration] sec"
  }

}
