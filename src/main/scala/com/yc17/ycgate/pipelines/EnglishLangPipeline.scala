package com.yc17.ycgate.pipelines

import com.yc17.ycgate.sesame._
import com.yc17.ycgate.GateConfig.gateHome
import com.yc17.ycgate.prs.common.AnnotationSetTransfer
import com.yc17.ycgate.prs.en._

object EnglishLangPipeline extends Pipeline {
  override def plugins: List[String] = List(
    "ANNIE", "Tools", "Tagger_Numbers", "JAPE_Plus"
  )

  override def prs = List(
    AnnieTokeniser(),
    SentenceSplitter(),
    AnnieGazetteer(),
    NumbersTagger(),
    YCGazetteer(),
    POSTagger.LowerCase(),
    POSTagger.UpperCase(),
    Morpher(),
    ANNIENamedEntityTransducer(),
    YCJape()
  )

  def apply() = {
    SesameConditionalSerialAnalyserController(
      gateHome.toString, "English Language Pipeline", SesameOutputType.Xml
    ).addPlugins(plugins).addPRs(prs)()
  }
}
