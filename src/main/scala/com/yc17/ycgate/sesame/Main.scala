package com.yc17.ycgate.sesame

import SesameOutputType._
import gate.Document
import gate.AnnotationSet

object Main {
  implicit val outputTypeRead: scopt.Read[SesameOutputType] =
    scopt.Read.reads(SesameOutputType withName _)

  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[SesameConfig]("sesame") {
      head("Sesame 1.0.1")
      opt[String]('h', "gateHome") action { (v, c) =>
        c.copy(gateHomePath = Some(v))
      } text("Location of the gate home")
      opt[String]('g', "gappFile") action { (v, c) =>
        c.copy(gappFile = Some(v))
      } text("Location of the gapp file")
      opt[String]('f', "file") action { (v, c) =>
        c.copy(filePath = Some(v))
      } text("Location of the file to be processed")
      opt[SesameOutputType]('t', "type") action { (v, c) =>
        c.copy(outputFormat = v)
      } text("Output format. Supported values (case sensitive): Xml, Doc, Annotations")
      help("help") text("prints this usage text")
    }

    parser.parse(args, SesameConfig()) match {
      case Some(SesameConfig(Some(gapp), Some(home), Some(file), outputType)) =>
        val result = run(gapp, home, file, outputType)
        println(s"${result}")
      case Some(SesameConfig(None, _, _, _)) =>
        println(s"gapp file is not specified.")
      case Some(SesameConfig(_, None, _, _)) =>
        println(s"gate home is not specified.")
      case Some(SesameConfig(_, _, None, _)) =>
        println(s"file to be processed is not specified.")
      case None =>
    }
  }

  private def run(gapp: String, home: String, file: String, t: SesameOutputType): String = {
    val sesame = SesameSerialAnalyserController(home, t)(gapp)
    t match {
      case Annotations => sesame.processFile[AnnotationSet](file) {_.getAnnotations}.toString
      case Doc         => sesame.processFile[Document](file)(identity).toString
      case Xml         => sesame.processFile[String](file) {_.toXml}
    }
  }
}

case class SesameConfig(
  gappFile: Option[String] = None,
  gateHomePath: Option[String] = None,
  filePath: Option[String] = None,
  outputFormat: SesameOutputType = Xml
)
