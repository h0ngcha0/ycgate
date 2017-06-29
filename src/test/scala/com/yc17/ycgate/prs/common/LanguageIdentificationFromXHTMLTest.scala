package com.yc17.ycgate.prs.common

import com.yc17.ycgate.sesame._
import org.scalatest.FunSpec
import org.scalatest.Matchers
import java.net.URI
import java.net.URL
import java.nio.file.Paths
import com.yc17.ycgate.GateConfig.gateHome
import java.nio.file.{Paths, Path}

class LanguageIdentificationFromXHTMLTest extends FunSpec
    with Matchers
    with SesameControllerTestDSL
{
  override def gateHomeURL = gateHome.toUri.toURL
  override def prs = List(
    LanguageIdentificationFromXHTML()
  )

  val refStr = "This is a language sensitive text"

  // NOTE: This suite sometimes fail non-deterministically. Not sure
  //       if it is an race condition.
  describe("Run LanguageIdentification.jape") {

    it("should identify en language") {
      List("en", "sv", "cn").foreach { verifyLanguageFeature _ }
    }

    it("should identify en language if meta.name is not set to Content-Language") {
      val whole = (refStr <<= "meta") >>- Feature("lang", "en")
      whole.verify()
    }
  }

  private def verifyLanguageFeature(lang: String): Unit = {
    val whole = (refStr <<= "meta" ^ Feature("name", "Content-Language") ^
                                     Feature("content", lang)) >>- Feature("lang", lang)
    whole.verify()
  }
}
