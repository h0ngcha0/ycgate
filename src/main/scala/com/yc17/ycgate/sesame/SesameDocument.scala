package com.yc17.ycgate.sesame

import gate.Document
import gate.Factory
import gate.FeatureMap
import gate.corpora.DocumentImpl
import java.lang.Boolean

case class SesameDocument(
  content: String,
  extraDocFeatures: FeatureMap = Factory.newFeatureMap(),
  annotations: List[SesameAnnotation] = Nil
){
  val docParams: FeatureMap = Factory.newFeatureMap()
  docParams.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, content)
  docParams.put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, "text/html")
  docParams.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8")

  val gateDocument: Document = Factory.createResource(
    classOf[DocumentImpl].getName, docParams).asInstanceOf[Document]

  gateDocument.setMarkupAware(true)

  val origAnnots = gateDocument.getAnnotations
  annotations.foreach { a =>
    origAnnots.add(a.id, a.startOffset, a.endOffset, a.name, a.features)
  }

  val docFeatures = gateDocument.getFeatures
  docFeatures.putAll(extraDocFeatures)

  def toGateDocument: Document = gateDocument
}
