package functions.remote.sbt

import functions.proxygenerator.sbt.{SbtCallerParams, SbtReceiverParams}

object Builders {
  def sbtCallerParams(
      avroSerialization: Boolean = false,
      jsonSerialization: Boolean = false,
      classloaderTransport: Boolean = false,
      http4sClientTransport: Boolean = false,
      targetDir: String = "/tmp/sbt-caller-test-dir",
      exportDependency: String = ""
  ) = SbtCallerParams(
    avroSerialization,
    jsonSerialization,
    classloaderTransport,
    http4sClientTransport,
    targetDir,
    exportDependency
  )

  def sbtReceiverParams(
      avroSerialization: Boolean = false,
      jsonSerialization: Boolean = false,
      http4sRoutes: Boolean = false,
      targetDir: String = "/tmp/sbt-receiver-test-dir",
      exportDependency: String = ""
  ) = SbtReceiverParams(
    avroSerialization,
    jsonSerialization,
    http4sRoutes,
    targetDir,
    exportDependency
  )
}
