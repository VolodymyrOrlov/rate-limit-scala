name := "rate-limit-scala"

organization := "com.vorlov.ratelimit"

version := "1.0.1"

scalaVersion := "2.11.8"

lazy val http4sVersion = "0.14.0-SNAPSHOT"

lazy val slf4jVersion = "1.7.14"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "org.apache.commons" % "commons-lang3" % "3.4",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.h2database" % "h2" % "1.4.191",
  "biz.paluch.redis" % "lettuce" % "4.1.1.Final",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)