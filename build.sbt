ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.gu"
ThisBuild / organizationName := "The Guardian"

lazy val root = (project in file("."))
  .settings(
    name := "aws-missing-alarm-audit",
    description := "Does AWS CloudFormation declares a Lambda or EC2 resource without any alarms?",
    libraryDependencies ++= Seq(
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "com.lihaoyi" %% "upickle" % "1.1.0",
      "com.gu" %% "spy" % "0.1.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    )
  )
