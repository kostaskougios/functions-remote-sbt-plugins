package functions.remote.sbt

import com.sksamuel.avro4s.{AvroOutputStream, Encoder}
import functions.coursier.CoursierResolver
import functions.proxygenerator.sbt.SbtCallerParams
import functions.remote.utils.ClassLoaderUtils
import org.apache.commons.io.output.ByteArrayOutputStream

import java.net.URLClassLoader

class FunctionsRemoteIsolatedExecutor(coursierResolver: CoursierResolver) {
  def generateCaller(p: SbtCallerParams): String =
    isolatedLoadAndApply[String](
      Artifacts.FunctionsRemoteGeneratorArtifact,
      "functions.proxygenerator.sbt.SbtCaller",
      toByteArray(p)
    )

  private def toByteArray[A: Encoder](a: A) = {
    val bos = new ByteArrayOutputStream
    val aos = AvroOutputStream.data[A].to(bos).build()
    aos.write(a)
    aos.close()
    bos.toByteArray
  }

  private def isolatedLoadAndApply[R](dependency: String, className: String, data: Array[Byte]) = {
    val files = coursierResolver.resolveDependency(dependency)
    val cl    = new URLClassLoader(files.map(_.toURI.toURL).toArray, null)
    ClassLoaderUtils.withThreadContextClassLoader(cl) {
      val sbtCaller =
        cl.loadClass(className)
          .getDeclaredConstructor()
          .newInstance()
          .asInstanceOf[java.util.function.Function[Array[Byte], R]]

      sbtCaller(data)
    }
  }
}

object FunctionsRemoteIsolatedExecutor {
  val Instance = new FunctionsRemoteIsolatedExecutor(new CoursierResolver())
}
