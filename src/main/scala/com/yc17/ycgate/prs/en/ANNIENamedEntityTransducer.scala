package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameTransducer
import com.yc17.ycgate.GateConfig.pluginHome
import java.nio.file.Paths

object ANNIENamedEntityTransducer {
  def apply() = {
    SesameTransducer(
      Paths.get(pluginHome.toString, "ANNIE", "resources", "NE", "main.jape")
        .toUri.toURL.toString
    )
  }
}
