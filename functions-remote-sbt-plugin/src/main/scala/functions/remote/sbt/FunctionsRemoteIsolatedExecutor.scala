package functions.remote.sbt

import com.sksamuel.avro4s.{AvroOutputStream, Encoder}
import functions.coursier.CoursierResolver
import functions.proxygenerator.sbt.{SbtCallerParams, SbtReceiverParams}
import functions.remote.utils.ClassLoaderUtils
import org.apache.commons.io.output.ByteArrayOutputStream

import java.net.URLClassLoader

class FunctionsRemoteIsolatedExecutor(coursierResolver: CoursierResolver) {
  def resolve(deps: Seq[String]): Seq[String] = coursierResolver.createDependenciesForArtifacts(deps)

  def generateCaller(p: SbtCallerParams): String = {
    resolve(Seq(p.exportDependency))
    isolatedLoadAndApply[String](
      "functions.proxygenerator.sbt.SbtCaller",
      toByteArray(p)
    )
  }

  def generateReceiver(p: SbtReceiverParams): String = {
    resolve(Seq(p.exportDependency))
    isolatedLoadAndApply[String](
      "functions.proxygenerator.sbt.SbtReceiver",
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

  private def generatorClassLoader                                          = {
    val files = coursierResolver.resolveDependency(Artifacts.FunctionsRemoteGeneratorArtifact)
    new URLClassLoader(files.map(_.toURI.toURL).toArray, null)
  }
  private def isolatedLoadAndApply[R](className: String, data: Array[Byte]) = {
    // We won't reuse the classloader because the loaded tasty-inspector will load
    // jars in mem and keep them until sbt exits. This means any updates to the
    // jars are not loaded if the classloader is the same.
    val cl = generatorClassLoader
    try
      ClassLoaderUtils.withThreadContextClassLoader(cl) {
        val sbtCaller =
          cl
            .loadClass(className)
            .getDeclaredConstructor()
            .newInstance()
            .asInstanceOf[java.util.function.Function[Array[Byte], R]]

        sbtCaller(data)
      }
    finally cl.close()
  }
}

object FunctionsRemoteIsolatedExecutor {
  val Instance = new FunctionsRemoteIsolatedExecutor(new CoursierResolver())
}
