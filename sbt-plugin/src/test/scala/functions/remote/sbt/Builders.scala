package functions.remote.sbt

import functions.proxygenerator.sbt.SbtCallerParams

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
}
