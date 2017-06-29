package com.yc17.ycgate.pipelines

import com.yc17.ycgate.sesame._
import org.scalatest.FunSpec
import org.scalatest.Matchers

// Run LanguageIdentification.jape and NotLangIdentification.jape in a row
class LanguageModulePipelineTest extends FunSpec
    with Matchers
    with SesameControllerTestDSL {
  override implicit lazy val controller = LanguageModulePipeline()

  describe("Run both LanguageIdentification.jape and NotLangIdentification.jape in order") {
    it("should set the value of doc feature 'language_supported' to 'true' for supported languages") {
      List("en", "cn", "sv").foreach { testString(_, true).verify() }
    }

    it("should set the value of doc feature 'language_supported' to 'false' for unsupported languages") {
      List("de", "es", "fr").foreach { testString(_, false).verify() }
    }
  }

  private def testString(lang: String, supported: Boolean) = {
        val refStr = "i am a piece of language sensitive text"
        (refStr <<= "html" <<= "meta" ^ Feature("name", "Content-Language") ^
                                        Feature("content", lang)
        ) >>- Feature("language_supported", supported)
  }
}

