package com.yc17.ycgate.sesame

import gate.Annotation
import gate.FeatureMap
import gate.Factory
import gate.Document
import gate.AnnotationSet
import gate.CorpusController
import gate.creole.ConditionalSerialAnalyserController
import gate.creole.SerialController
import java.net.URI
import java.net.URL
import java.nio.file.Path
import org.scalatest.FunSpec

import org.scalatest.Matchers
import scala.util.Try

import scala.collection.JavaConversions._

import scala.language.implicitConversions

import scalaz._
import Scalaz._

trait SesameControllerTestDSL { self: FunSpec with Matchers =>
  lazy private val gateHome = new URI(gateHomeURL.toString).getPath

  implicit lazy val controller = SesameConditionalSerialAnalyserController(gateHome, SesameOutputType.Xml)
    .addPlugins(plugins)
    .addPRs(prs)()

  def gateHomeURL: URL = Thread.currentThread().getContextClassLoader().getResource("GATE")
  def prs: List[SesameProcessingResource] = List()
  def plugins: List[String] = List("ANNIE", "Tools") // by default, load "ANNIE" and "Tools"

  case class Feature(
    name: String,
    value: Any = null
  )

  case class DocumentAnnotation(
    id: Int,
    name: String,
    startOffset: Int,
    endOffset: Int,
    features: Option[List[Feature]] // None means the annotation is absent
  ) {
    def shiftOffset(offset: Int) = {
      copy(
        startOffset= startOffset + offset,
        endOffset= endOffset + offset
      )
    }

    // annotation contains a specific feature
    def ^(feature: Feature): DocumentAnnotation = {
      val newFs = features.map { fs => feature :: fs }.getOrElse(List(feature))
      copy(features = Some(newFs))
    }

    // annotation doesn't contain a specific feature
    def !^(feature0: Feature): DocumentAnnotation = {
      val feature = feature0.copy(value = null) // value null means absent
      this.^(feature)
    }

    // annotation doesn't contain any features
    def !!^(): DocumentAnnotation = {
      copy(features = None)
    }
  }


  trait TestSupport {
    val inputDocumentFeatures: List[Feature]
    val inputDocumentAnnotations: List[DocumentAnnotation]
    val outputDocumentFeatures: List[Feature]
    val outputPresentDocumentAnnotations: List[DocumentAnnotation]
    val outputAbsentDocumentAnnotations: List[DocumentAnnotation]

    val stringUnderTest: String

    // short hand
    def str = stringUnderTest


    def verify(
      verifier: (Document) => Unit = { processedDoc =>
        verifyDocumentFeatures(processedDoc, outputDocumentFeatures)
        verifyPresentAnnotations(processedDoc, outputPresentDocumentAnnotations)
        verifyAbsentAnnotations(processedDoc, outputAbsentDocumentAnnotations)
      }
    )(implicit controller: SesameController[ConditionalSerialAnalyserController]) = {

      // try to print the test report
      controller.process[Unit](
        SesameDocument(
          stringUnderTest,
          toFeatureMap(Some(inputDocumentFeatures)),
          toSesameAnnotations(inputDocumentAnnotations)
        )
      )((printTestDocument _).map(verifier))
    }

    def verifyWithoutOffset()(
      implicit controller: SesameController[ConditionalSerialAnalyserController]
    ) = {
      verify(
        { processedDoc =>
          verifyDocumentFeatures(processedDoc, outputDocumentFeatures)
          verifyPresentAnnotations(processedDoc, outputPresentDocumentAnnotations, false)
          verifyAbsentAnnotations(processedDoc, outputAbsentDocumentAnnotations, false)
        }
      )
    }

    def verifyDebug() = {
      verify { processedDoc =>
        val annotations = processedDoc.getAnnotations
        println(s"annots: $annotations")
      }
    }

    private def toSesameAnnotations(
      docAnnotations: List[DocumentAnnotation]
    ): List[SesameAnnotation] = {
      docAnnotations.zip((0 until docAnnotations.length).toList).map {
        case (annotation, id) => {
          val featureMap = toFeatureMap(annotation.features)
          SesameAnnotation(
            id, annotation.startOffset, annotation.endOffset,
            annotation.name, featureMap
          )
        }
      }
    }

    private def toFeatureMap(docFeatures: Option[List[Feature]]): FeatureMap = {
      val featureMap: FeatureMap = Factory.newFeatureMap()
      docFeatures.map { df =>
        df.foreach { feature =>
          featureMap.put(feature.name, feature.value.asInstanceOf[Object])
        }
      }
      featureMap
    }

    private def verifyDocumentFeatures(
      doc: Document,
      features: List[Feature]
    ): Unit = {
      val docFeatures = doc.getFeatures
      verifyFeatures(docFeatures, features)
    }

    private def verifyPresentAnnotations(
      doc: Document,
      expectedPresentAnnotations: List[DocumentAnnotation],
      withOffset: Boolean = true
    ): Unit = {
      val annotationSet = doc.getAnnotations
      expectedPresentAnnotations.foreach { expectedAnnotation =>
        val annotation = retrieveAnnotation(
          annotationSet, expectedAnnotation, withOffset
        ) match {
          case scala.util.Success(annot) => annot
          case scala.util.Failure(e: java.util.NoSuchElementException) => None
          case scala.util.Failure(e) => throw(e)
        }

        // if features of the expected annotation is null, this verifies
        // the absence of the feature.
        expectedAnnotation.features.map { f =>
          val name = expectedAnnotation.name
          val eStart = expectedAnnotation.startOffset
          val eEnd = expectedAnnotation.endOffset
          assert(
            !annotation.isEmpty,
            s"annotation '${name}' *is not* found at ($eStart, $eEnd)"
          )
          annotation.map( a => verifyFeatures(a.getFeatures(), f) )
        }.getOrElse {
          assert(annotation.isEmpty)
        }
      }
    }

    private def verifyAbsentAnnotations(
      doc: Document,
      expectedAbsentAnnotations: List[DocumentAnnotation],
      withOffset: Boolean = true
    ): Unit = {
      val annotationSet = doc.getAnnotations
      expectedAbsentAnnotations.foreach { expectedAbsentAnnotation =>
        val annotation = retrieveAnnotation(
          annotationSet, expectedAbsentAnnotation, withOffset
        ) match {
          case scala.util.Success(annot) => annot
          case scala.util.Failure(e: java.util.NoSuchElementException) => None
          case scala.util.Failure(e) => throw(e)
        }

        val name = expectedAbsentAnnotation.name
        val eStart = expectedAbsentAnnotation.startOffset
        val eEnd = expectedAbsentAnnotation.endOffset
        assert(
          annotation.isEmpty,
          s"annotation '${name}' *is* found at ($eStart, $eEnd)"
        )
      }
    }

    private def retrieveAnnotation(
      annotationSet: AnnotationSet,
      expectedAnnotation: DocumentAnnotation,
      withOffset: Boolean
    ): Try[Option[Annotation]] = {
      Try {
        // if an annotation starts and ends at the same place,
        // annotationSet.get(name, startOffset, endOffset)
        // will not return it. that is why it is commented out.
        withOffset.option(
          annotationSet.get(
            expectedAnnotation.name,
            expectedAnnotation.startOffset,
            expectedAnnotation.endOffset
          )
        ).getOrElse(
          annotationSet.get(
            expectedAnnotation.name
          )
        ).iterator().toList.find { a =>
          a.getStartNode.getOffset == expectedAnnotation.startOffset &&
          a.getEndNode.getOffset == expectedAnnotation.endOffset
        }
      }
    }

    private def verifyFeatures(
      featureMap: FeatureMap,
      features: List[Feature]
    ): Unit = {
      features.foreach { feature =>
        assert(
          featureMap.get(feature.name) == feature.value,
          s"feature '${feature.name}''s value is not equal to ${feature.value}"
        )
      }
    }

    private def printTestDocument(processedDoc: Document): Document = {
      val content = processedDoc.getContent.toString
      info(s"Given a string: ${content}")
      info("before processing...")
      printFeatures(inputDocumentFeatures, "input document level features is:")
      printPresentAnnotation(inputDocumentAnnotations, content, "input annotation set is:")
      info("after processing...")
      printFeatures(outputDocumentFeatures, "output document level features is:")
      printPresentAnnotation(outputPresentDocumentAnnotations, content, "output annotation set is:")
      printAbsentAnnotation(outputAbsentDocumentAnnotations, content, "output absent annotation set is:")
      processedDoc
    }

    private def printFeatures(features: List[Feature], msg: String): Option[Unit] =
      (!features.isEmpty).option(features).map { fs => info(s"$msg"); info(s"${fs.mkString("[ ", ", ", " ]")}")}

    private def printPresentAnnotation(
      annots: List[DocumentAnnotation],
      content: String,
      msg: String
    ): Option[Unit] = {
      (!annots.isEmpty).option(annots).map { as =>
        info(s"$msg")
        as.foreach { annot =>
          val (start, end) = (annot.startOffset, annot.endOffset)
          val str = content.substring(start, end)
          val features = annot.features.map {
            case List() => "with no features."
            case fs     => s"with the following features: ${fs.mkString("[ ", ", ", " ]")}."
          }.getOrElse("with no features.")

          info(s"`$str` ($start, $end) is annotated as `${annot.name}` $features")
        }
      }
    }

    private def printAbsentAnnotation(
      annots: List[DocumentAnnotation],
      content: String,
      msg: String
    ): Option[Unit] = {
      (!annots.isEmpty).option(annots).map { as =>
        info(s"$msg")
        as.foreach { annot =>
          val (start, end) = (annot.startOffset, annot.endOffset)
          val str = content.substring(start, end)
          info(s"`$str` ($start, $end) is NOT annotated as `${annot.name}`")
        }
      }
    }
  }

  case class TestDoc(
    documentPath: Path,
    val inputDocumentFeatures: List[Feature] = List(),
    val inputDocumentAnnotations: List[DocumentAnnotation] = List(),
    val outputDocumentFeatures: List[Feature] = List(),
    val outputPresentDocumentAnnotations: List[DocumentAnnotation] = List(),
    val outputAbsentDocumentAnnotations: List[DocumentAnnotation] = List()
  ) extends TestSupport {
    val stringUnderTest = scala.io.Source.fromFile(documentPath.toString, "utf-8").mkString

    def inputDocumentFeature(
      features: Feature*
    ) = {
      copy(
        inputDocumentFeatures =
          inputDocumentFeatures ++ features
      )
    }

    def outputDocumentFeature(feature: Feature*) = {
      copy(
        outputDocumentFeatures =
          outputDocumentFeatures ++ feature
      )
    }

    def inputDocumentAnnotation(
      annotations: DocumentAnnotation*
    ) = {
      copy(
        inputDocumentAnnotations =
          inputDocumentAnnotations ++ annotations
      )
    }

    def outputDocumentAnnotation(
      annotations: DocumentAnnotation*
    ) = {
      copy(
        outputPresentDocumentAnnotations =
          outputPresentDocumentAnnotations ++ annotations
      )
    }

    def outputAbsentDocumentAnnotation(
      annotations: DocumentAnnotation*
    ) = {
      copy(
        outputAbsentDocumentAnnotations =
          outputAbsentDocumentAnnotations ++ annotations
      )
    }
  }

  case class TestString(
    val stringUnderTest: String,
    val stringUnderTestLength: Option[Int] = None,
    val inputDocumentFeatures: List[Feature] = List(),
    val inputDocumentAnnotations: List[DocumentAnnotation] = List(),
    val outputDocumentFeatures: List[Feature] = List(),
    val outputPresentDocumentAnnotations: List[DocumentAnnotation] = List(),
    val outputAbsentDocumentAnnotations: List[DocumentAnnotation] = List()
  ) extends TestSupport {

    def strLength(length: Int) = copy(stringUnderTestLength = Some(length))
    def strLength = stringUnderTestLength.getOrElse(stringUnderTest.length)

    def inputDocumentFeature(
      features: Feature*
    ) = {
      copy(
        inputDocumentFeatures =
          inputDocumentFeatures ++ features
      )
    }

    def outputDocumentFeature(
      feature: Feature*
    ) = {
      copy(
        outputDocumentFeatures =
          outputDocumentFeatures ++ feature
      )
    }

    def inputDocumentAnnotation(
      annotations: DocumentAnnotation*
    ) = {
      copy(
        inputDocumentAnnotations =
          inputDocumentAnnotations ++ annotations
      )
    }

    def outputDocumentAnnotation(
      annotations: DocumentAnnotation*
    ) = {
      copy(
        outputPresentDocumentAnnotations =
          outputPresentDocumentAnnotations ++ annotations
      )
    }

    def outputAbsentDocumentAnnotation(
      annotations: DocumentAnnotation*
    ) = {
      copy(
        outputAbsentDocumentAnnotations =
          outputAbsentDocumentAnnotations ++ annotations
      )
    }

    def <<-(feature: Feature) = inputDocumentFeature(feature)
    def >>-(feature: Feature) = outputDocumentFeature(feature)
    def <<=(annotation: DocumentAnnotation) = {
      inputDocumentAnnotation(annotation.copy(
        startOffset = 0, endOffset = strLength
      ))
    }
    def >>=(annotation: DocumentAnnotation) = {
      outputDocumentAnnotation(annotation.copy(
        startOffset = 0, endOffset = strLength
      ))
    }
    def !>>=(annotation: DocumentAnnotation) = {
      outputAbsentDocumentAnnotation(annotation.copy(
        startOffset = 0, endOffset = strLength
      ))
    }

    def +(newTestString: TestString): TestString = {
      val currentLength = strLength
      val newInputAnnotations = newTestString.inputDocumentAnnotations.map(_.shiftOffset(currentLength))
      val newOutputAnnotations = newTestString.outputPresentDocumentAnnotations.map(_.shiftOffset(currentLength))
      copy(
        stringUnderTest = stringUnderTest ++ newTestString.stringUnderTest,
        stringUnderTestLength = Some(strLength + newTestString.strLength),
        inputDocumentAnnotations = inputDocumentAnnotations ++ newInputAnnotations,
        outputPresentDocumentAnnotations = outputPresentDocumentAnnotations ++ newOutputAnnotations
      )
    }

    def |+|(newTestString: TestString): TestString = this.+(" ") + newTestString
    // `+` and `|+|` have different operator precedence, chaining them together is sometimes
    // problematic. `|+` is a alias to `+` but has the same operator precedence as `|+|`
    def |+(newTestString: TestString): TestString = this.+(newTestString)
  }

  // Enable conversion of a String to a TestString case class
  implicit def stringToTestString(str: String) = TestString(str)
  // Enable conversion of a String to a DocumentAnnotation case class
  implicit def stringToDocumentAnnotation(str: String) = {
    DocumentAnnotation(1, str, 0, 0, Some(List()))
  }

  // Enrich a few token methods for string
  implicit class TokenStr(str: String) {
    def token = str <<= "Token" ^ Feature("string", s"$str")
    def token(caseSpec: String) =
      str <<= "Token" ^ Feature("string", s"$str") ^ Feature("orth", caseSpec)

    def upperInitToken = token("upperInitial")
    def allCapsToken = token("allCaps")
    def numberToken = str <<= "Token" ^ Feature("kind", "number")
    def textToken = str <<="Token" ^ Feature("Type", "Text")
    def tokenWithRoot(root: String) = str <<= "Token" ^ Feature("root", root)
    def tokenWithRoot: TestString = tokenWithRoot(str)
    def markup = str.strLength(0)
    // create a sequence of tokens, with single space in between
    def tokens = str.split(" ").map(_.token).reduce(_ |+| _)
    def tokens(caseSpec: String) = str.split(" ").map(_.token(caseSpec)).reduce(_ |+| _)
  }

  implicit class LookupStr(str: String) {
    def lookup = str <<= "Lookup"
    def lookupMajor(majorType: String) =
      str <<= "Lookup" ^ Feature("majorType", majorType)
    def lookupMinor(minorType: String) =
      str <<= "Lookup" ^ Feature("minorType", minorType)
  }

  val comma = ",".token
  val ORIGINAL_MARKUPS = "Original markups"
}
