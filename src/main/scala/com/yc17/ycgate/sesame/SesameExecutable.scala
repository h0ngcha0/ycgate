package com.yc17.ycgate.sesame

import gate.Gate
import gate.LanguageAnalyser
import gate.util.persistence.PersistenceManager
import gate.creole.SerialController
import gate.Annotation
import gate.AnnotationSet
import gate.Corpus
import gate.CorpusController
import gate.Document
import gate.Factory
import gate.FeatureMap
import gate.corpora.DocumentImpl
import gate.corpora.CorpusImpl
import gate.creole.ResourceInstantiationException
import gate.util.OffsetComparator
import org.slf4j.LoggerFactory
import java.lang.Boolean

trait SesameExecutable[+T <: SerialController with CorpusController] {
  val controller: Option[T] = None
  val logger = LoggerFactory.getLogger(this.getClass)

  def processFile[T](file: String)(f: (Document) => T): T = {
    import scala.io.Source

    process[T](Source.fromFile(file).mkString)(f)
  }


  def process[T](doc: Document)(f: (Document) => T): T = controller match {
    case None => throw new SesameException("controller not initialised")
    case Some(ctl) =>
      val corpus: Corpus = Factory.createResource(
        classOf[CorpusImpl].getName).asInstanceOf[Corpus]

      corpus.add(doc)

      ctl.setCorpus(corpus)
      logger.info("Executing the pipeline. Be patient, this might take a while...")
      ctl.execute()

      val result = f(doc)

      corpus.unloadDocument(doc)
      Factory.deleteResource(doc)
      Factory.deleteResource(corpus)

      result
  }

  def process[T](doc: SesameDocument)(f: (Document) => T): T = {
    val gateDocument = doc.toGateDocument
    process(gateDocument)(f)
  }

  def process[T](content: String)(f: (Document) => T): T = {
    val gateDocument = SesameDocument(content).toGateDocument
    process(gateDocument)(f)
  }
}
