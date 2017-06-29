package com.yc17.ycgate.prs.en

import com.yc17.ycgate.sesame.SesameProcessingResource
import com.yc17.ycgate.GateConfig.pluginHome
import java.nio.file.Paths

object POSTagger {
  object LowerCase {
    def apply() = posTagger("lexicon", "ruleset")
  }

  object UpperCase {
    def apply() = posTagger("lexicon_cap", "rules_cap")
  }

  private def posTagger(lexicon: String, rule: String) = {
    SesameProcessingResource(
      "gate.creole.POSTagger",
      "ANNIE LowerCase POSTagger",
      Map(
        "lexiconURL"  -> annieHeptagResource("lexicon"),
        "rulesURL"    -> annieHeptagResource("ruleset")
      )
    )
  }

  private def annieHeptagResource(file: String): String = {
    Paths
      .get(pluginHome.toString, "ANNIE", "resources", "heptag", file)
      .toUri
      .toString
  }
}
