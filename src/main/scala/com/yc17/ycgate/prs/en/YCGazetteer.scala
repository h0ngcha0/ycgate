package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameGazetteer
import com.yc17.ycgate.GateConfig.gazetteerHome
import java.nio.file.Paths

// PR for the main custom gazetteer files
object YCGazetteer {
  def apply() = {
    val listsUrlPath = Paths
      .get(gazetteerHome.toString, "en", "lists.def")
      .toUri.toURL

    SesameGazetteer(listsUrlPath, "\t", Map("wholeWordsOnly"->"false"))
  }
}
