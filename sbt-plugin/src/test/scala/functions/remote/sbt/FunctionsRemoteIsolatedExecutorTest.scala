package functions.remote.sbt

import functions.coursier.CoursierResolver
import functions.remote.sbt.Builders.{sbtCallerParams, sbtReceiverParams}
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers.*

class FunctionsRemoteIsolatedExecutorTest extends AnyFunSuiteLike {
  val TestExport = "com.example:ls-exports_3:0.1-SNAPSHOT"

  val resolver = new CoursierResolver()
  resolver.createDependenciesForArtifacts(Seq(TestExport))
  val executor = new FunctionsRemoteIsolatedExecutor(resolver)

  test("invocation of generateCaller") {
    executor.generateCaller(
      sbtCallerParams(
        avroSerialization = true,
        jsonSerialization = true,
        classloaderTransport = true,
        http4sClientTransport = true,
        exportDependency = TestExport
      )
    ) should be("OK")
  }

  test("invocation of generateReceiver") {
    executor.generateReceiver(
      sbtReceiverParams(
        avroSerialization = true,
        jsonSerialization = true,
        http4sRoutes = true,
        exportDependency = TestExport
      )
    )
  }
}
