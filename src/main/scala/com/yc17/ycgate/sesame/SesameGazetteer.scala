package com.yc17.ycgate.sesame

import gate.Factory
import gate.LanguageAnalyser
import gate.creole.gazetteer.DefaultGazetteer
import java.net.URL
import scalaz.Scalaz._

case class SesameGazetteer(
  val gazetteerFilePath: URL,
  val seperator: String = ":",  // one char string, default to ":"
  val extraInitParams: Map[String, Object] = Map()
) extends SesameProcessingResource {
  val defaultInitParams = Map(
    "listsURL"      -> gazetteerFilePath,
    "encoding"      -> "UTF-8",
    "caseSensitive" -> "false",
    "gazetteerFeatureSeparator" -> seperator
  )

  override def className = classOf[DefaultGazetteer].getName
  // Override default if it has to merge keys.
  override def initParams =
    (extraInitParams.mapValues(List(_)) |+| defaultInitParams.mapValues(List(_))).mapValues(_.head)
}

object SesameGazetteer {
  def apply(path: String): SesameGazetteer = apply(path, ":")

  def apply(path: String, seperator: String): SesameGazetteer =
    SesameGazetteer(new URL(path), seperator)

  def apply(path: String, extraParams: Map[String, Object]): SesameGazetteer =
    SesameGazetteer(new URL(path), ":", extraParams)
}
