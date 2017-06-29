import sbt._
import Keys._
import Common._
import java.nio.file.Paths

object Gate {
  // Specify the name of the plugins that're shipped with official gate distribution.
  private val officialPluginsWhitelist = List("Tools", "ANNIE", "Tagger_Numbers", "JAPE_Plus")

  // gate home
  val homePath = Paths.get(classPathRoot.toString, "home")
  // location for the shared libs between creole components
  val libDir = Paths.get(homePath.toString, "lib").toString
  // location for the data (models, etc) for creole components
  val dataDir = Paths.get(homePath.toString, "data").toString

  lazy val project = Project(
    "gate",
    file("project/gate")
  ).configs(config("gateSrc")).settings(commonSettings).settings(
    populateGateOfficialPlugins := Def.task {
      val updateReport = (update in Compile).value
      val tempPath = Paths.get(homePath.toString, "tmp").toString
      val tempDir = new File(tempPath)
      IO.createDirectory(new File(libDir))
      IO.createDirectory(tempDir)
      IO.unzipURL(new URL("https://bintray.com/liuhongchao/maven/download_file?file_path=gate-8.1-build5169-SRC.jar"), tempDir)
      officialPluginsWhitelist.foreach { plugin =>
        val pluginSrcDir = new File(Paths.get(tempPath.toString, "gate-8.1-build5169-SRC", "plugins", plugin).toString)
        val pluginTargetDir = new File(Paths.get(homePath.toString, "plugins", plugin).toString)
        IO.copyDirectory(pluginSrcDir, pluginTargetDir, true, true)
      }
      IO.delete(tempDir);
    }.value,
    publish <<= Def.task {
      logging(s"Gate plugins $officialPluginsWhitelist published")
    } dependsOn(populateGateOfficialPlugins),
    publishLocal <<= publish
  )

  private lazy val populateGateOfficialPlugins = taskKey[Unit]("Task that populates the gate official plugins")

  // Make sure the plugin dir structure exists
  def ensurePluginDir(packageName: String, subDir: String = "") = {
    val relPluginDirName = packageName.takeWhile{ p => p != '.' && p != '-'}
    val path = Paths.get(homePath.toString, "plugins", relPluginDirName, subDir)
    val dir = new File(path.toString)
    IO.createDirectory(dir)

    dir
  }
}
