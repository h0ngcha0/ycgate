package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameGazetteer
import com.yc17.ycgate.GateConfig._
import java.nio.file.Paths

object AnnieGazetteer {
  def apply() = {
    val listsUrlPath = Paths
      .get(pluginHome.toString, "ANNIE", "resources", "gazetteer", "lists.def")
      .toUri
      .toString

    SesameGazetteer(listsUrlPath, "\t")
  }
}
