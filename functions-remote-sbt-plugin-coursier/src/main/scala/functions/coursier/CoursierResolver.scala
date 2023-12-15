package functions.coursier
import coursier._
import functions.coursier.utils.Env.FunctionsHome
import functions.coursier.utils.FileUtils

import java.io.File

/** see https://get-coursier.io/docs/api
  */
class CoursierResolver(functionsHome: String = FunctionsHome) {
  def createDependenciesForArtifacts(artifacts: Seq[String]): Seq[String] = {
    if (artifacts.nonEmpty) {
      val targetDir = new File(functionsHome + "/local/dependencies")
      targetDir.mkdirs()
      for (artifact <- artifacts) yield {
        val r      = resolveDependency(artifact)
        val output = r.mkString("\n")
        FileUtils.writeTextFile(targetDir, s"$artifact.classpath", output)
        s"$targetDir/$artifact.classpath"
      }
    } else Nil
  }

  def resolveDependency(dependency: String): Seq[File] = resolve(toDependency(dependency))

  private def resolve(dependency: Dependency) = Fetch()
    .addDependencies(dependency)
    .run()

  private def toDependency(dep: String) = {
    val (groupId, artifactId, version) = dep.split(":") match {
      case Array(groupId, artifactId, version) => (groupId, artifactId, version)
      case _ => throw new IllegalArgumentException(s"Can't parse dependency $dep, it should be in the format of group:artifact:version")
    }
    Dependency(Module(Organization(groupId), ModuleName(artifactId)), version)
  }
}
