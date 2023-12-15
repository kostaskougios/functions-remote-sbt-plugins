ThisBuild / publishMavenStyle      := true
ThisBuild / Test / publishArtifact := false
ThisBuild / pomIncludeRepository   := { _ => false }
ThisBuild / licenses               := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / homepage               := Some(url("https://github.com/kostaskougios/functions-remote-sbt-plugins"))
ThisBuild / scmInfo                := Some(
  ScmInfo(
    url("https://github.com/kostaskougios/functions-remote-sbt-plugins"),
    "scm:https://github.com/kostaskougios/functions-remote-sbt-plugins.git"
  )
)
ThisBuild / developers             := List(
  Developer(id = "kostaskougios", name = "Kostas Kougios", email = "kostas.kougios@googlemail.com", url = url("https://github.com/kostaskougios"))
)
ThisBuild / versionScheme          := Some("early-semver")

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
