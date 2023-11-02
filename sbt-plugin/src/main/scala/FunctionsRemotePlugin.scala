import functions.proxygenerator.sbt.SbtCallerParams
import functions.remote.sbt.FunctionsRemoteIsolatedExecutor
import sbt.*
import sbt.Keys.*

object FunctionsRemotePlugin extends AutoPlugin {
  // https://www.scala-sbt.org/1.x/docs/Plugins.html
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = noTrigger // Not automatically enabled

  object autoImport {
    val functionsRemoteReceiver               = settingKey[Boolean]("Set to true to generate Receiver classes")
    val functionsRemoteCreateDependenciesFile = taskKey[Unit]("Creates dependency text file under ~/.functions-remote")
    val functionsRemoteGenerateCaller         = taskKey[Unit]("Generates caller classes")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    functionsRemoteReceiver               := false,
    Compile / unmanagedSourceDirectories ++= {
      val base = baseDirectory.value
      if (functionsRemoteReceiver.value)
        Seq(base / "src" / "main" / "functions-remote-generated")
      else Seq.empty
    },
    functionsRemoteGenerateCaller         := {
      val executor = FunctionsRemoteIsolatedExecutor.Instance
      executor.generateCaller(
        SbtCallerParams(
          false,
          false,
          false,
          false,
          "/tmp",
          ""
        )
      )
    },
    functionsRemoteCreateDependenciesFile := {
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
    publishLocal                          := Def.taskDyn {
      val s  = streams.value
      val pl = publishLocal.value
      Def.task {
        s.log.info(pl.toString)
        // Run the task actions here after publishLocal has completed
        functionsRemoteCreateDependenciesFile.value
        pl
      }
    }.value
  )
}
