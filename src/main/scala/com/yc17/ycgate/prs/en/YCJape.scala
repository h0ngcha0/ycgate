package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameTransducer
import com.yc17.ycgate.GateConfig.japeHome
import java.nio.file.Paths
import java.util.Arrays

object YCJape {
  def apply() = {
    SesameTransducer(Paths.get(japeHome.toString, "en", "main.jape").toUri.toURL.toString)
  }
}
