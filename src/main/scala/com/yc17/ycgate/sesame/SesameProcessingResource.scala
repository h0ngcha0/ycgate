package com.yc17.ycgate.sesame

import gate.LanguageAnalyser
import gate.Factory

trait SesameProcessingResource {
  def className: String
  val resourceName: Option[String] = None
  def initParams: Map[String, Object] = Map()   // override this to have more params
  val featuresMap: Map[String, Object] = Map() // override this to have more features
  val runOptions: RunOptions = AlwaysRun

  def create(): LanguageAnalyser = {
    val paramsList = initParams.flatMap { case (k, v) => List(k, v) }.toArray
    val featuresList = featuresMap.flatMap { case (k, v) => List(k, v) }.toArray

    Factory.createResource(
      className,
      gate.Utils.featureMap(paramsList:_*),
      gate.Utils.featureMap(featuresList:_*),
      resourceName.getOrElse(null)
    ).asInstanceOf[LanguageAnalyser]
  }
}

object SesameProcessingResource {
  def apply(
    classN: String,
    resourceN: String,
    iMap: Map[String, Object] = Map(),
    fMap: Map[String, Object] = Map(),
    runOpt: RunOptions = AlwaysRun
  ) = {
    new SesameProcessingResource {
      override val className = classN
      override val resourceName = Some(resourceN)
      override def initParams = iMap
      override val featuresMap = fMap
      override val runOptions = runOpt
    }
  }
}

trait RunOptions
case object AlwaysRun extends RunOptions
case object NeverRun extends RunOptions
case class ConditionalRun(
  featureName: String, featureValue: String
) extends RunOptions
