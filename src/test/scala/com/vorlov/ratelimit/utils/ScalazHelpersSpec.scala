package com.vorlov.ratelimit.utils

import ScalazHelpers._

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import scalaz.concurrent.Task

class ScalazHelpersSpec extends WordSpec with Matchers {

  "ScalazHelpers" should {

    "implicitly apply scalaFuture2scalazTask function to convert future to task" in {

      val converted: Task[String] = Future.successful("")

      converted shouldBe a [Task[_]]

    }

    "implicitly apply scalaTry2scalazTask function to convert try to task" in {

      val converted: Task[String] = Try("")

      converted shouldBe a [Task[_]]

    }

  }

}
