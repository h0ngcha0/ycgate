import sbt._
import Keys._
import Common._
import java.nio.file.Paths
import java.nio.charset.Charset

trait GatePlugin {
  val publishGatePluginTask = Def.task {
    val resourcesDir = (resourceDirectory in Compile).value
    val codeArtifact = (artifact in (Compile, packageBin)).value
    val pluginResourcesDir = Gate.ensurePluginDir(codeArtifact.name, "resources")

    IO.copyDirectory(resourcesDir, pluginResourcesDir)
    val from  = Paths.get(pluginResourcesDir.getCanonicalPath)
    val creoleFile = new File(from.toString ++ "/creole.xml");
    IO.copyFile(creoleFile, new File(from.toString++ "/../creole.xml"), true)
    IO.delete(creoleFile);

    (packagedArtifacts in (Compile, packageBin) map { artifacts: Map[Artifact, File]=>
      artifacts.map { case (art, file) => (art.name, art.`type`, art.classifier, file) }.map {
        case (name, "jar", None, file) =>
          // no classifier, the normal jar file.
          val newPluginDir = Gate.ensurePluginDir(name)
          val targetFile = Paths.get(newPluginDir.getCanonicalPath, file.getName)
          IO.copyFile(file, new File(targetFile.toString), false)
          logging(s"processed jar file: $file")
        case (name, _, _, file) =>
      }

      logging("done copying the creole component")
    }).value
  }
}
