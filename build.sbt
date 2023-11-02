ThisBuild / version := "0.1-SNAPSHOT"

ThisBuild / organization := "org.functions-remote"

name := "functions-remote-sbt-plugins"

ThisBuild / scalaVersion := "2.12.18"

val Coursier  = "io.get-coursier" %% "coursier"  % "2.1.7"
val ScalaTest = "org.scalatest"   %% "scalatest" % "3.2.15" % Test

lazy val `sbt-plugin` = project
  .settings(
    pluginCrossBuild / sbtVersion := "1.2.8"
  )
  .enablePlugins(SbtPlugin)
  .dependsOn(coursier)

lazy val coursier = project
  .settings(
    libraryDependencies ++= Seq(Coursier, ScalaTest),
    Compile / packageDoc / publishArtifact := false
  )
