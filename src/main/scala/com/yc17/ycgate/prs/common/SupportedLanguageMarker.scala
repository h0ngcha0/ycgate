package com.yc17.ycgate.prs.common

import com.yc17.ycgate.sesame.SesameTransducer
import com.yc17.ycgate.GateConfig._
import java.nio.file.Paths

// Set the `language_supported` feature to true if language is supported,
// otherwise false.
object SupportedLanguageMarker {
  def apply() = {
    SesameTransducer(
      Paths.get(japeHome.toString, "common", "SupportedLanguageMarker.jape").toUri.toString,
      "Supported Language Marker"
    )
  }
}
