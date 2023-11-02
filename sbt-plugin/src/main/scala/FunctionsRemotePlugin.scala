import functions.proxygenerator.sbt.SbtCallerParams
import functions.remote.sbt.FunctionsRemoteIsolatedExecutor
import sbt.*
import sbt.Keys.*

object FunctionsRemotePlugin extends AutoPlugin {
  // https://www.scala-sbt.org/1.x/docs/Plugins.html
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = noTrigger // Not automatically enabled

  object autoImport {
    object functionsRemoteReceiver {
      val exports           = settingKey[Seq[String]]("Add all exports that you need receivers to be generated")
      val avroSerialization = settingKey[Boolean]("Set to true to generate Avro serializers classes")
    }
    val functionsRemoteCreateDependenciesFile = taskKey[Unit]("Creates dependency text file under ~/.functions-remote")
    val functionsRemoteGenerate = taskKey[Unit]("Generates caller classes")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    functionsRemoteReceiver.exports           := Nil,
    functionsRemoteReceiver.avroSerialization := false,
    Compile / unmanagedSourceDirectories ++= {
      val base = baseDirectory.value
      if (functionsRemoteReceiver.exports.value.nonEmpty)
        Seq(base / "src" / "main" / "functions-remote-generated")
      else Seq.empty
    },
    cleanFiles += baseDirectory.value / "src" / "main" / "functions-remote-generated",
    functionsRemoteGenerate                   := {
      val executor = FunctionsRemoteIsolatedExecutor.Instance
      val base     = baseDirectory.value
      val s        = streams.value
      for (exp <- functionsRemoteReceiver.exports.value) {
        s.log.info(s"Generating receiver for $exp")
        executor.generateCaller(
          SbtCallerParams(
            functionsRemoteReceiver.avroSerialization.value,
            false,
            false,
            false,
            (base / "src" / "main" / "functions-remote-generated").getAbsolutePath,
            exp
          )
        )
      }
    },
    functionsRemoteCreateDependenciesFile     := {
      val s        = streams.value
      val org      = organization.value
      val sv       = scalaBinaryVersion.value
      val n        = name.value + "_" + sv.takeWhile(_ != '.')
      val v        = version.value
      val artifact = s"$org:$n:$v"
      s.log.info(s"functions-remote will create dependency file for $artifact")

      functions.coursier.Resolver.createDependenciesForArtifact(artifact)
    },
    // compile should generate code where appropriate
    Compile / compile                         := (Compile / compile dependsOn functionsRemoteGenerate).value,
    // hook into publishLocal so that after publishing we create functions-remote dependency file
    publishLocal                              := Def.taskDyn {
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
