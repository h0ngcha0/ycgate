package com.yc17.ycgate.sesame

import gate.creole.Transducer
import java.net.URL
import scalaz.Scalaz._

case class SesameTransducer(
  val japeFile: URL,
  val extraInitParams: Map[String, Object] = Map(),
  override val featuresMap: Map[String, Object] = Map(),
  override val resourceName: Option[String] = None,
  override val runOptions: RunOptions = AlwaysRun
) extends SesameProcessingResource {
  val defaultInitParams = Map(
    "grammarURL" -> japeFile,
    "encoding"   -> "UTF-8"
  )

  override def className = classOf[Transducer].getName

  // Override default if it has to merge keys.
  override def initParams =
    (extraInitParams.mapValues(List(_)) |+| defaultInitParams.mapValues(List(_))).mapValues(_.head)
}

object SesameTransducer {
  def apply(path: String):SesameTransducer = SesameTransducer(new URL(path))
  def apply(
    path: String,
    initParams: Map[String, Object],
    extraFeatures: Map[String, Object]
  ):SesameTransducer = SesameTransducer(new URL(path), extraFeatures, initParams)

  def apply(
    path: String,
    resourceName: String
  ):SesameTransducer = SesameTransducer(new URL(path), Map(), Map(), Some(resourceName))

  def apply(
    path: String,
    runOption: RunOptions
  ):SesameTransducer = SesameTransducer(new URL(path), Map(), Map(), None, runOption)
}
