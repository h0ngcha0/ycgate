package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameProcessingResource
import com.yc17.ycgate.GateConfig._
import java.nio.file.Paths

object Morpher {
  def apply() = {
    SesameProcessingResource(
      "gate.creole.morph.Morph",
      "EN Morph analyzer",
      Map(
        "rulesFile"     -> morphResource("default.rul"),
        "caseSensitive" -> "false"
      )
    )
  }

  private def morphResource(file: String): String = {
    Paths.get(
      pluginHome.toString, "Tools", "resources", "morph", file
    ).toUri.toString
  }
}
