package functions.remote.sbt

import functions.coursier.CoursierResolver
import functions.remote.sbt.Builders.sbtCallerParams
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers.*

class FunctionsRemoteIsolatedExecutorTest extends AnyFunSuiteLike {
  val TestExport = "com.example:ls-exports_3:0.1-SNAPSHOT"

  val executor = new FunctionsRemoteIsolatedExecutor(new CoursierResolver())
  val p        = sbtCallerParams(
    avroSerialization = true,
    jsonSerialization = true,
    classloaderTransport = true,
    http4sClientTransport = true,
    exportDependency = TestExport
  )

  test("loads proxy generator") {
    executor.generateCaller(p) should be("OK")
  }
}
