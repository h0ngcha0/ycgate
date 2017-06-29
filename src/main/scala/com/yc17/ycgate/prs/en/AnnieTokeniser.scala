package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameProcessingResource
import com.yc17.ycgate.GateConfig._
import java.nio.file.Paths

object AnnieTokeniser {
  def apply() = {

    SesameProcessingResource(
      "gate.creole.tokeniser.DefaultTokeniser",
      "ANNIE English Tokeniser",
      Map(
        "tokeniserRulesURL"    -> annieTokeniserResource("DefaultTokeniser.rules"),
        "transducerGrammarURL" -> annieTokeniserResource("postprocess.jape"),
        "encoding"             -> "UTF-8"
      )
    )
  }

  private def annieTokeniserResource(file: String): String = {
    Paths
      .get(pluginHome.toString, "ANNIE", "resources", "tokeniser", file)
      .toUri
      .toString
  }
}
