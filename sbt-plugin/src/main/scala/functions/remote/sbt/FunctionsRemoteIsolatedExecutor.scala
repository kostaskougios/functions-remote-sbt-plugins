package functions.remote.sbt

import com.sksamuel.avro4s.AvroOutputStream
import functions.coursier.CoursierResolver
import functions.proxygenerator.sbt.SbtCallerParams
import org.apache.commons.io.output.ByteArrayOutputStream

import java.net.URLClassLoader

class FunctionsRemoteIsolatedExecutor(coursierResolver: CoursierResolver) {
  def runGenerator() = {
    val files     = coursierResolver.resolveDependency(Artifacts.FunctionsRemoteGeneratorArtifact)
    val cl        = new URLClassLoader(files.map(_.toURI.toURL).toArray, null)
    val sbtCaller =
      cl.loadClass("functions.proxygenerator.sbt.SbtCaller")
        .getDeclaredConstructor()
        .newInstance()
        .asInstanceOf[java.util.function.Function[Array[Byte], String]]

    val bos    = new ByteArrayOutputStream
    val aos    = AvroOutputStream.data[SbtCallerParams].to(bos).build()
    aos.write(SbtCallerParams())
    aos.close()
    val result = sbtCaller(bos.toByteArray)
    println(result)
  }
}

object FunctionsRemoteIsolatedExecutor {
  val Instance = new FunctionsRemoteIsolatedExecutor(new CoursierResolver())
}
