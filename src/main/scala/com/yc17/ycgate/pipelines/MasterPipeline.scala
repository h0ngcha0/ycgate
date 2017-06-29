package com.yc17.ycgate.pipelines

import gate.Document
import com.yc17.ycgate.sesame.SesameDocument
import com.typesafe.scalalogging.slf4j.LazyLogging

// Master pipeline assembles a bunch of pipeline together.
// As long as it defines a method that can be used to process
// a document, it should be ok to call it a pipeline.

// MasterPipeline expose the same interface as SesameExecutable

object MasterPipeline extends LazyLogging {
  private val languageModulePipeline = LanguageModulePipeline()
  private val enPipeline = EnglishLangPipeline()

  def process[T](inputDoc: Document, lang: Option[String] = None)(
    f: (Document) => T
  ): T = {
    languageModulePipeline.process(inputDoc) { doc =>
      lang.orElse(Option(doc.getFeatures.get("lang")).map(_.toString.toLowerCase)) match {
        case Some("en") => enPipeline.process(doc)(f)
        case Some(lang) => {
          val errStr = s"language: [$lang] is not supported"
          logger.error(errStr); throw new LanguageNotSupportedException(errStr)
        }
        case None       => throw new LanguageNotIdentifiedException()
      }
    }
  }

  def processFile[T](file: String, lang: Option[String] = None)(
    f: (Document) => T
  ): T = {
    import scala.io.Source

    process[T](Source.fromFile(file).mkString, lang)(f)
  }

  def process[T](doc: SesameDocument, lang: Option[String])(
    f: (Document) => T
  ): T = {
    val gateDocument = doc.toGateDocument
    process(gateDocument, lang)(f)
  }

  def process[T](content: String, lang: Option[String])(
    f: (Document) => T
  ): T = {
    val gateDocument = SesameDocument(content).toGateDocument
    process(gateDocument, lang)(f)
  }
}

class LanguageNotSupportedException(msg: String) extends Exception {}
class LanguageNotIdentifiedException() extends Exception {}
