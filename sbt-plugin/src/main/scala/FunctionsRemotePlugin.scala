import functions.proxygenerator.sbt.{SbtCallerParams, SbtReceiverParams}
import functions.remote.sbt.FunctionsRemoteIsolatedExecutor
import sbt.*
import sbt.Keys.*

object FunctionsRemotePlugin extends AutoPlugin {
  // https://www.scala-sbt.org/1.x/docs/Plugins.html
  override def requires = sbt.plugins.JvmPlugin
  override def trigger  = noTrigger // Not automatically enabled

  object autoImport {
    val callerExports                 = settingKey[Seq[String]]("Add all exports that you need callers to be generated")
    val callerAvroSerialization       = settingKey[Boolean]("Set to true to generate Avro serializers")
    val callerJsonSerialization       = settingKey[Boolean]("Set to true to generate Json serializers")
    val callerClassloaderTransport    =
      settingKey[Boolean]("Set to true to generate a transport that uses an isolated class loader to load and execute functions")
    val callerClassloaderDependencies = settingKey[Seq[String]]("All dependencies (impl of functions) that the classloader transport needs to execute")
    val callerHttp4sClientTransport   = settingKey[Boolean]("Set to true to generate a transport using an http4s client")

    val receiverExports           = settingKey[Seq[String]]("Add all exports that you need receivers to be generated")
    val receiverAvroSerialization = settingKey[Boolean]("Set to true to generate Avro serializers")
    val receiverJsonSerialization = settingKey[Boolean]("Set to true to generate Json serializers")
    val receiverHttp4sRoutes      = settingKey[Boolean]("Set to true to generate code for http4s routes")

    val functionsRemoteCreateDependenciesFile = taskKey[Unit]("Creates dependency text file under ~/.functions-remote")
    val functionsRemoteGenerate               = taskKey[Unit]("Generates caller classes")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    // caller
    callerExports                         := Nil,
    callerAvroSerialization               := false,
    callerJsonSerialization               := false,
    callerClassloaderTransport            := false,
    callerClassloaderDependencies         := Nil,
    callerHttp4sClientTransport           := false,
    // receiver
    receiverExports                       := Nil,
    receiverAvroSerialization             := false,
    receiverJsonSerialization             := false,
    receiverHttp4sRoutes                  := false,
    // source directories for generated files
    Compile / unmanagedSourceDirectories ++= {
      val base = baseDirectory.value
      if (callerExports.value.nonEmpty || receiverExports.value.nonEmpty)
        Seq(base / "src" / "main" / "functions-remote-generated")
      else Seq.empty
    },
    // clean does clean generated source dirs
    cleanFiles += baseDirectory.value / "src" / "main" / "functions-remote-generated",
    // generate source files as per configuration of a project
    functionsRemoteGenerate               := {
      val executor  = FunctionsRemoteIsolatedExecutor.Instance
      val base      = baseDirectory.value
      val s         = streams.value
      val targetDir = base / "src" / "main" / "functions-remote-generated"
      if (!targetDir.exists() || targetDir.list().isEmpty) {
        for (exp <- callerExports.value) {
          s.log.info(s"Generating caller for $exp")
          executor.generateCaller(
            SbtCallerParams(
              avroSerialization = callerAvroSerialization.value,
              jsonSerialization = callerJsonSerialization.value,
              classloaderTransport = callerClassloaderTransport.value,
              http4sClientTransport = callerHttp4sClientTransport.value,
              targetDir = targetDir.getAbsolutePath,
              exportDependency = exp
            )
          )
        }
        executor.resolve(callerClassloaderDependencies.value)
        for (exp <- receiverExports.value) {
          s.log.info(s"Generating receiver for $exp")
          executor.generateReceiver(
            SbtReceiverParams(
              avroSerialization = receiverAvroSerialization.value,
              jsonSerialization = receiverJsonSerialization.value,
              http4sRoutes = receiverHttp4sRoutes.value,
              targetDir = targetDir.getAbsolutePath,
              exportDependency = exp
            )
          )
        }
      } else
        s.log.info(
          s"functions-remote: Won't generate code for ${name.value} to speed up compilation. Please do an sbt clean or delete all generated classes if you want code to be re-generated"
        )
    },
    // create a dependency file under ~/.functions-remote-config
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
    // compile should generate code where appropriate
    Compile / compile                     := (Compile / compile dependsOn functionsRemoteGenerate).value
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
