package functions.proxygenerator.sbt

case class SbtCallerParams(
    avroSerialization: Boolean = false,
    jsonSerialization: Boolean = false,
    classloaderTransport: Boolean = false,
    http4sClientTransport: Boolean = false
)
