package com.yc17.ycgate.prs.common

import com.yc17.ycgate.sesame.SesameTransducer
import com.yc17.ycgate.GateConfig.japeHome
import java.nio.file.Paths

object LanguageIdentificationFromXHTML {
  def apply() = {
    SesameTransducer(
      Paths.get(japeHome.toString, "common", "LanguageIdentification.jape").toUri.toString,
      "Language Identification From XHTML"
    )
  }
}
