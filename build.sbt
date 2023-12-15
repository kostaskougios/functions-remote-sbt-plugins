ThisBuild / version := "0.1-SNAPSHOT"

ThisBuild / organization := "io.github.kostaskougios"

name := "functions-remote-sbt-plugins"

ThisBuild / scalaVersion := "2.12.18"

val Coursier  = "io.get-coursier"     %% "coursier"    % "2.1.7"
val Avro4s    = "com.sksamuel.avro4s" %% "avro4s-core" % "4.1.1"
val ScalaTest = "org.scalatest"       %% "scalatest"   % "3.2.15" % Test

lazy val `functions-remote-sbt-plugin` = project
  .settings(
    pluginCrossBuild / sbtVersion := "1.2.8",
    libraryDependencies ++= Seq(ScalaTest, Avro4s)
  )
  .enablePlugins(SbtPlugin)
  .dependsOn(`functions-remote-sbt-plugin-coursier`)

lazy val `functions-remote-sbt-plugin-coursier` = project
  .settings(
    libraryDependencies ++= Seq(Coursier, ScalaTest),
    Compile / packageDoc / publishArtifact := false
  )
