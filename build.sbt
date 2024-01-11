name := """VoiceAPP"""
organization := "Personal"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
libraryDependencies += "com.twilio.sdk" % "twilio" % "8.0.0"




// Adds additional packages into Twirl
//TwirlKeys.templateImports += "Personal.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "Personal.binders._"
