package com.yc17.ycgate.pipelines

import com.yc17.ycgate.sesame._
import java.io.File
import org.scalatest.FunSpec
import org.scalatest.Matchers
import scala.io.Source

class EnglishLangPipelineSpec extends FunSpec
    with Matchers
    with SesameControllerTestDSL
{
  describe("sanity check on the english pipeline.") {
    it("should run english pipeline") {
      val file = new File(getClass.getResource("/documents/test_document.html").toURI)
      EnglishLangPipeline().process(Source.fromFile(file.getAbsolutePath).mkString) { doc =>
        assert(doc.getAnnotationSetNames.contains("Original markups"))
      }
    }
  }
}
