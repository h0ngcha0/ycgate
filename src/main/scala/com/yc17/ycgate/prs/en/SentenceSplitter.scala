package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameProcessingResource
import com.yc17.ycgate.GateConfig.pluginHome
import java.net.URL
import java.nio.file.Paths

object SentenceSplitter {
  def apply() = {
    SesameProcessingResource(
      "gate.creole.splitter.RegexSentenceSplitter",
      "English Sentence Splitter",
      Map(
        "encoding"              -> "UTF-8",
        "externalSplitListURL"  -> splitterUrl("external-split-patterns.txt"),
        "nonSplitListURL"       -> splitterUrl("non-split-patterns.txt"),
        "internalSplitListURL"  -> splitterUrl("internal-split-patterns.txt")
      ),
      Map()
    )
  }

  private def splitterUrl(file: String): URL = {
    Paths.get(
      pluginHome.toString, "ANNIE", "resources", "regex-splitter", file
    ).toUri.toURL
  }
}
