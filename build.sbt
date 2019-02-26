import sbt.Resolver

name := """gpx-analyzer"""
version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.5"

resolvers += Resolver.jcenterRepo
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


libraryDependencies ++= Seq(
  // Common
  "net.codingwell" %% "scala-guice" % "4.1.0",

  //Web
  "org.webjars" %% "webjars-play" % "2.6.1",
  "org.webjars" % "jquery" % "3.2.1",
  "org.webjars" % "bootstrap" % "4.0.0-beta.2" exclude("org.webjars", "jquery"),
  "org.webjars" % "chartjs" % "2.7.2",
  //"org.webjars.bower" % "popper.js" % "1.12.3" exclude("org.webjars", "jquery"), - doesn't work

  // Database
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.typesafe.play" %% "play-slick" % "3.0.3",

  // Testing
  "org.scalatest" % "scalatest_2.12" % "3.2.0-SNAP9",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2",

  specs2 % Test,
  guice,
  jdbc
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  //"-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  // Play has a lot of issues with unused imports and unsued params
  // https://github.com/playframework/playframework/issues/6690
  // https://github.com/playframework/twirl/issues/105
  "-Xlint:-unused,_"
)
