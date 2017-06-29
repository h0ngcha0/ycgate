package com.yc17.ycgate.prs.common

import com.yc17.ycgate.sesame._
import org.scalatest.FunSpec
import org.scalatest.Matchers
import java.net.URI
import java.net.URL
import java.nio.file.Paths
import com.yc17.ycgate.GateConfig.gateHome
import java.nio.file.{Paths, Path}

class SupportedLanguageMarkerTest extends FunSpec
    with Matchers
    with SesameControllerTestDSL
{
  override def gateHomeURL = gateHome.toUri.toURL
  override def prs = List(
    SupportedLanguageMarker()
  )

  describe("Run SupportedLanguageMarkerTest.jape") {
    it("should set doc feature 'language_supported' to true for supported languages") {
      List("en", "sv").foreach { lang =>
        (testString(Some(lang)) >>- Feature("language_supported", true)).verify()
      }
    }

    it("should set doc feature 'language_supported' to false for unsupported languages") {
      List("de", "dk", "fr").foreach { lang =>
        (testString(Some(lang)) >>- Feature("language_supported", false)).verify()
      }
    }

    it("should set doc feature 'language_supported' to false if lang feature not found") {
      (testString(None) >>- Feature("language_supported", false)).verify()
    }
  }

  private def testString(lang: Option[String]) = {
    val refStr = "this is a language sentitive text"
    (lang.map { refStr <<- Feature("lang", _) }
      getOrElse { refStr <<- Feature("lang", null) }) <<= "html"
  }
}
