package com.vorlov.ratelimit.service.impl

import com.lambdaworks.redis.RedisClient
import com.vorlov.ratelimit.model.RateLimitRule
import com.vorlov.ratelimit.service.RateLimitService

import scala.util.Try
import scalaz.concurrent.Task

import scala.collection.JavaConversions._
import com.vorlov.ratelimit.utils.ScalazHelpers._

/**
  * An implementation of [[RateLimitService]] that uses Redis and breaks continuous time into
  * a discrete set of buckets, each holding a count of requests per user.
  */
class RedisRateLimitService extends RateLimitService {

  private val bucketCap = 20000
  private val bucketStep = 5
  private val bucketValidityPeriod = 86400
  private val bucketsNumber = math.round(bucketCap / bucketStep.toDouble)
  private val keyPrefix = "requests"

  private val redisClient = RedisClient.create("redis://localhost:6379")

  /**
    *
    * @inheritdoc
    */
  override def report(userID: String): Task[Unit] = Try {

    val bucketNumber = bucket()

    val redisConnection = redisClient.connect()

    redisConnection.sync().multi()

    val key = s"$keyPrefix:$userID"

    redisConnection.sync().hincrby(key, bucketNumber.toString, 1)

    redisConnection.sync().hdel(key, ((bucketNumber + 1) % bucketsNumber).toString)
    redisConnection.sync().hdel(key, ((bucketNumber + 2) % bucketsNumber).toString)

    redisConnection.sync().expire(key, bucketValidityPeriod)

    redisConnection.sync().exec()

    redisConnection.close()

  }

  /**
    *
    * @inheritdoc
    */
  override  def verify(userID: String, policies: Seq[RateLimitRule]): Task[Seq[String]] = Try {
    policies.flatMap {
      policy =>
        if(count(userID, policy.duration) >= policy.maxRequests){
          Some(s"Number of maximum requests reached in [${policy.description}]")
        } else None
    }
  }

  private def count(userID: String, interval: Long): Long = {

    var bucketNumber = bucket()

    val redisConnection = redisClient.connect()

    redisConnection.sync().multi()

    val key = s"$keyPrefix:$userID"

    redisConnection.sync().hget(key, bucketNumber.toString)

    (math.floor(interval / bucketStep.toDouble) to 0 by -1).map{
      count =>
        bucketNumber -= 1
        val bucket = (bucketNumber + bucketsNumber) % bucketsNumber
        redisConnection.sync().hget(key, bucket.toString)
    }

    val result = redisConnection.sync().exec().map {
      r => Option(r).map(_.toString.toInt).getOrElse(0)
    }.sum

    redisConnection.close()

    result

  }

  private def bucket(): Long = {
    val time = System.currentTimeMillis / 1000
    math.floor((time % bucketCap) / bucketStep).toLong
  }

}
