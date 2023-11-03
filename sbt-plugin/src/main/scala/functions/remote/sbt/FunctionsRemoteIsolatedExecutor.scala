package functions.remote.sbt

import com.sksamuel.avro4s.{AvroOutputStream, Encoder}
import functions.coursier.CoursierResolver
import functions.proxygenerator.sbt.SbtCallerParams
import functions.remote.utils.ClassLoaderUtils
import org.apache.commons.io.output.ByteArrayOutputStream

import java.net.URLClassLoader

class FunctionsRemoteIsolatedExecutor(coursierResolver: CoursierResolver) {
  def generateCaller(p: SbtCallerParams): String = {
    coursierResolver.createDependenciesForArtifacts(Seq(p.exportDependency))
    isolatedLoadAndApply[String](
      "functions.proxygenerator.sbt.SbtCaller",
      toByteArray(p)
    )
  }

  private def toByteArray[A: Encoder](a: A) = {
    val bos = new ByteArrayOutputStream
    val aos = AvroOutputStream.data[A].to(bos).build()
    aos.write(a)
    aos.close()
    bos.toByteArray
  }

  private lazy val generatorClassLoader                                     = {
    val files = coursierResolver.resolveDependency(Artifacts.FunctionsRemoteGeneratorArtifact)
    new URLClassLoader(files.map(_.toURI.toURL).toArray, null)
  }
  private def isolatedLoadAndApply[R](className: String, data: Array[Byte]) = {
    ClassLoaderUtils.withThreadContextClassLoader(generatorClassLoader) {
      val sbtCaller =
        generatorClassLoader
          .loadClass(className)
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
