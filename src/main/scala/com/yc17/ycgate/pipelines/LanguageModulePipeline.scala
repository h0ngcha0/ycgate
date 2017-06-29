package com.yc17.ycgate.pipelines

import com.yc17.ycgate.sesame._
import com.yc17.ycgate.prs.common._
import com.yc17.ycgate.GateConfig.gateHome
import java.nio.file.Path

object LanguageModulePipeline extends Pipeline {
  override def plugins: List[String] = List("ANNIE", "Tools")

  override def prs = List(
    LanguageMetaTransfer(),
    LanguageIdentificationFromXHTML(),
    SupportedLanguageMarker()
  )

  def apply() = {
    SesameConditionalSerialAnalyserController(
      gateHome.toString, "Yc Language Module", SesameOutputType.Xml
    ).addPlugins(plugins).addPRs(prs)()
  }
}
