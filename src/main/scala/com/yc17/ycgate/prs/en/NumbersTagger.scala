package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameProcessingResource
import com.yc17.ycgate.GateConfig.pluginHome
import java.net.URL
import java.nio.file.Paths
import java.nio.file.Path

object NumbersTagger {
  def apply() = {
    SesameProcessingResource(
      "gate.creole.numbers.NumbersTagger",
      "English Sentence Splitter",
      Map(
        "encoding"              -> "UTF-8",
        "configURL"             -> configUrl("all.xml"),
        "postProcessURL"        -> postProcessUrl
      ),
      Map()
    )
  }

  // file string could be:
  // all.xml/english.xml/french.xml/german.xml/spanish.xml/dutch.xml/symbols.xml
  // where all.xml contains everything
  private def configUrl(file: String): URL = {
    resourcesPath.resolve("languages").resolve(file).toUri.toURL
  }

  private def postProcessUrl: URL = {
    resourcesPath.resolve("jape").resolve("post-process.jape").toUri.toURL
  }

  private def resourcesPath: Path = {
    Paths.get(pluginHome.toString, "Tagger_Numbers", "resources")
  }
}
