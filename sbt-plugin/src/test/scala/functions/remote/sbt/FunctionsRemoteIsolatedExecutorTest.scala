package functions.remote.sbt

import functions.coursier.CoursierResolver
import org.scalatest.funsuite.AnyFunSuiteLike

class FunctionsRemoteIsolatedExecutorTest extends AnyFunSuiteLike {
  val executor = new FunctionsRemoteIsolatedExecutor(new CoursierResolver())
  test("loads proxy generator") {
    executor.runGenerator()
  }
}
