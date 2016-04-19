package com.vorlov.ratelimit.utils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Failure, Success}
import scalaz.{-\/, \/-}
import scalaz.concurrent.Task

object ScalazHelpers {

  implicit def scalaFuture2scalazTask[T](fut: Future[T])(implicit ec: ExecutionContext): Task[T] = {
    Task.async {
      register =>
        fut.onComplete {
          case Success(v) => register(\/-(v))
          case Failure(ex) => register(-\/(ex))
        }
    }
  }

  implicit def scalaTry2scalazTask[T](tr: Try[T]): Task[T] = {
    tr match {
      case Success(v) => Task.now(v)
      case Failure(e) => Task.fail(e)
    }
  }

}
