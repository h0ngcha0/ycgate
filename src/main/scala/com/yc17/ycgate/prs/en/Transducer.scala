package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameTransducer
import com.yc17.ycgate.GateConfig.japeHome
import java.nio.file.Paths

trait Transducer {
  def japeFileName: String

  def apply() = {
    SesameTransducer(
      Paths.get(japeHome.toString, "en", japeFileName).toUri.toURL.toString
    )
  }
}
