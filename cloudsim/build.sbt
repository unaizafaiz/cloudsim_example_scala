name := "cloudsim"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++=  Seq (
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test",
  "junit" % "junit" % "4.12" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3" exclude ("org.slf4j", "slf4j-log4j12"),
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "com.typesafe" % "config" % "1.3.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.apache.commons" % "commons-math3" % "3.2"
  )