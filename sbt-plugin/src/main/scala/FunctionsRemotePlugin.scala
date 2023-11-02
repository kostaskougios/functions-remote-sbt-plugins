import sbt.*
import sbt.Keys.*

object FunctionsRemotePlugin extends AutoPlugin {
  // https://www.scala-sbt.org/1.x/docs/Plugins.html
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = noTrigger // Not automatically enabled

  object autoImport {
//    val helloGreeting = settingKey[String]("greeting")
    val functionsRemoteDependencies = taskKey[Unit]("say hello")
  }

  import autoImport.*

  override lazy val globalSettings: Seq[Setting[_]]  = Seq(
//    helloGreeting := "hi"
  )
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    functionsRemoteDependencies := {
      val s        = streams.value
      val org      = organization.value
      val sv       = scalaBinaryVersion.value
      val n        = name.value + "_" + sv.takeWhile(_ != '.')
      val v        = version.value
      val artifact = s"$org:$n:$v"
      s.log.info(s"functions-remote will create dependency file for $artifact")

      functions.coursier.Resolver.createDependenciesForArtifact(artifact)
    },
    // hook into publishLocal so that after publishing we create functions-remote dependency file
    publishLocal                := Def.taskDyn {
      val s  = streams.value
      val pl = publishLocal.value
      Def.task {
        s.log.info(pl.toString)
        // Run your task actions here after publishLocal has completed
        functionsRemoteDependencies.value
        pl
      }
    }.value
  )
}
