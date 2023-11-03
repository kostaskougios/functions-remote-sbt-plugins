package functions.proxygenerator.sbt

case class SbtReceiverParams(
    avroSerialization: Boolean,
    jsonSerialization: Boolean,
    http4sRoutes: Boolean,
    targetDir: String,
    exportDependency: String
)
