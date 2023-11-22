package functions.proxygenerator.sbt

case class SbtCallerParams(
    avroSerialization: Boolean,
    jsonSerialization: Boolean,
    classloaderTransport: Boolean,
    http4sClientTransport: Boolean,
    helidonClientTransport: Boolean,
    targetDir: String,
    exportDependency: String
)
