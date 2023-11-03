import functions.proxygenerator.sbt.{SbtCallerParams, SbtReceiverParams}
import functions.remote.sbt.FunctionsRemoteIsolatedExecutor
import sbt.*
import sbt.Keys.*

object FunctionsRemotePlugin extends AutoPlugin {
  // https://www.scala-sbt.org/1.x/docs/Plugins.html
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = noTrigger // Not automatically enabled

  object autoImport {
    object functionsRemoteCaller {
      val exports               = settingKey[Seq[String]]("Add all exports that you need callers to be generated")
      val avroSerialization     = settingKey[Boolean]("Set to true to generate Avro serializers")
      val jsonSerialization     = settingKey[Boolean]("Set to true to generate Json serializers")
      val classloaderTransport  = settingKey[Boolean]("Set to true to generate a transport that uses an isolated class loader to load and execute functions")
      val http4sClientTransport = settingKey[Boolean]("Set to true to generate a transport using an http4s client")
    }

    object functionsRemoteReceiver {
      val exports           = settingKey[Seq[String]]("Add all exports that you need receivers to be generated")
      val avroSerialization = settingKey[Boolean]("Set to true to generate Avro serializers")
      val jsonSerialization = settingKey[Boolean]("Set to true to generate Json serializers")
      val http4sRoutes      = settingKey[Boolean]("Set to true to generate code for http4s routes")
    }

    val functionsRemoteCreateDependenciesFile = taskKey[Unit]("Creates dependency text file under ~/.functions-remote")
    val functionsRemoteGenerate               = taskKey[Unit]("Generates caller classes")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    // caller
    functionsRemoteCaller.exports               := Nil,
    functionsRemoteCaller.avroSerialization     := false,
    functionsRemoteCaller.jsonSerialization     := false,
    functionsRemoteCaller.classloaderTransport  := false,
    functionsRemoteCaller.http4sClientTransport := false,
    // receiver
    functionsRemoteReceiver.exports             := Nil,
    functionsRemoteReceiver.avroSerialization   := false,
    functionsRemoteReceiver.jsonSerialization   := false,
    functionsRemoteReceiver.http4sRoutes        := false,
    // source directories for generated files
    Compile / unmanagedSourceDirectories ++= {
      val base = baseDirectory.value
      if (functionsRemoteCaller.exports.value.nonEmpty)
        Seq(base / "src" / "main" / "functions-remote-generated")
      else Seq.empty
    },
    // clean does clean generated source dirs
    cleanFiles += baseDirectory.value / "src" / "main" / "functions-remote-generated",
    // generate source files as per configuration of a project
    functionsRemoteGenerate                     := {
      val executor = FunctionsRemoteIsolatedExecutor.Instance
      val base     = baseDirectory.value
      val s        = streams.value
      for (exp <- functionsRemoteCaller.exports.value) {
        s.log.info(s"Generating caller for $exp")
        executor.generateCaller(
          SbtCallerParams(
            avroSerialization = functionsRemoteCaller.avroSerialization.value,
            jsonSerialization = functionsRemoteCaller.jsonSerialization.value,
            classloaderTransport = functionsRemoteCaller.classloaderTransport.value,
            http4sClientTransport = functionsRemoteCaller.http4sClientTransport.value,
            targetDir = (base / "src" / "main" / "functions-remote-generated").getAbsolutePath,
            exportDependency = exp
          )
        )
      }
      for (exp <- functionsRemoteReceiver.exports.value) {
        s.log.info(s"Generating receiver for $exp")
        executor.generateReceiver(
          SbtReceiverParams(
            avroSerialization = functionsRemoteReceiver.avroSerialization.value,
            jsonSerialization = functionsRemoteReceiver.jsonSerialization.value,
            http4sRoutes = functionsRemoteReceiver.http4sRoutes.value,
            targetDir = (base / "src" / "main" / "functions-remote-generated").getAbsolutePath,
            exportDependency = exp
          )
        )
      }
    },
    // create a dependency file under ~/.functions-remote-config
    functionsRemoteCreateDependenciesFile       := {
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
    Compile / compile                           := (Compile / compile dependsOn functionsRemoteGenerate).value
    // hook into publishLocal so that after publishing we create functions-remote dependency file
//    publishLocal                                := Def.taskDyn {
//      val s  = streams.value
//      val pl = publishLocal.value
//      Def.task {
//        s.log.info(pl.toString)
//        // Run the task actions here after publishLocal has completed
//        functionsRemoteCreateDependenciesFile.value
//        pl
//      }
//    }.value
  )
}
