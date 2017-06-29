package com.yc17.ycgate.sesame

import gate.Gate
import gate.creole.ConditionalSerialAnalyserController
import gate.util.persistence.PersistenceManager
import gate.creole.SerialAnalyserController
import gate.creole.SerialController
import gate.creole.AnalyserRunningStrategy
import gate.creole.RunningStrategy
import gate.Annotation
import gate.AnnotationSet
import gate.Corpus
import gate.CorpusController
import gate.Document
import gate.Factory
import gate.FeatureMap
import gate.LanguageAnalyser
import gate.corpora.DocumentImpl
import gate.corpora.CorpusImpl
import gate.creole.ResourceInstantiationException
import gate.util.OffsetComparator
import gate.util.persistence.PersistenceManager
import java.io.File
import java.lang.Boolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory

case class SesameController[+T <: SerialController with CorpusController](
  val gateHomePath: Option[String],
  val gatePluginsHome: Option[String],
  val className: String,
  val processingResources: List[SesameProcessingResource] = List(),
  val plugins: List[String] = List(),
  val outputType: SesameOutputType.SesameOutputType = SesameOutputType.Xml,
  override val controller: Option[T] = None,
  val controllerName: Option[String] = None
) extends SesameExecutable[T] {
  override val logger = LoggerFactory.getLogger(this.getClass)
  val dummyGateHome = "dummyGateHome"
  val dummyPlugingHome = "/plugins"
  val gateHomeDir = new File(gateHomePath.getOrElse(dummyGateHome))
  val gatePluginsHomeDir = new File(gatePluginsHome.getOrElse(gateHomeDir + dummyPlugingHome))

  def gateHome(homePath: String) = copy(gateHomePath = Some(homePath))
  def addPR(pr: SesameProcessingResource) = copy(
    // order has to be maintained
    processingResources = processingResources ++ List(pr)
  )
  def addPRs(prs: List[SesameProcessingResource]) = copy(
    processingResources = prs
  )
  def addPlugin(plugin: String) = copy(
    // order has to be maintained
    plugins = plugins ++ List(plugin)
  )
  def addPlugins(plugins0: List[String]) = copy(
    // order has to be maintained
    plugins = plugins0
  )

  def apply(gappFile: String) = {
    initGate()

    logger.info(s"Loading ${gappFile}...")
    val controller = PersistenceManager.loadObjectFromFile(
      new File(gateHomeDir, gappFile)
    ).asInstanceOf[T]

    copy(controller = Some(controller))
  }

  def apply() = {
    initGate()

    plugins.foreach { plugin =>
      println(s"plugin: $plugin")
      println(s"plugin home: ${Gate.getPluginsHome()}")
      Gate.getCreoleRegister().registerDirectories(
        new File(Gate.getPluginsHome(), plugin).toURI.toURL
      )
    }

    val controller = Factory.createResource(
      className,
      gate.Utils.featureMap(List().toArray:_*),  // potentially add params and features
      gate.Utils.featureMap(List().toArray:_*),
      controllerName.getOrElse(null)
    ).asInstanceOf[T]

    processingResources.zipWithIndex.foreach {
      case (pr, index) => {
        val strategy = pr.runOptions match {
          case AlwaysRun =>
            new AnalyserRunningStrategy(pr.create, RunningStrategy.RUN_ALWAYS, null, null)
          case NeverRun =>
            new AnalyserRunningStrategy(pr.create, RunningStrategy.RUN_NEVER, null, null)
          case ConditionalRun(name, value) =>
            new AnalyserRunningStrategy(pr.create, RunningStrategy.RUN_CONDITIONAL, name, value)
        }

        controller match {
          case c: ConditionalSerialAnalyserController => {
            c.add(index, strategy.getPR)
            c.setRunningStrategy(index, strategy)
          }
          case c: SerialAnalyserController => {
            c.add(index, strategy.getPR)
          }
          case t @ _ => {
            throw new SesameException(s"unsupported controller type: ${t.getClass}")
          }
        }
      }
    }


    copy(controller = Some(controller))
  }

  def toGapp(file: String): Unit = {
    controller.map(PersistenceManager.saveObjectToFile(_, new File(file))) orElse {
      throw new SesameException("controller not set")
    }
  }

  private def initGate() = {
    scala.util.Try {
      Gate.setGateHome(gateHomeDir)
      Gate.setPluginsHome(gatePluginsHomeDir)
    } recover {
      case e: java.lang.IllegalStateException =>
        logger.info(s"tolerate this error: $e")
    }

    Gate.init()
  }
}

object SesameConditionalSerialAnalyserController
    extends SerialAnalyserControllerObj[ConditionalSerialAnalyserController] {
  override def className = classOf[ConditionalSerialAnalyserController].getName
}

object SesameSerialAnalyserController
    extends SerialAnalyserControllerObj[SerialAnalyserController] {
  override def className = classOf[SerialAnalyserController].getName
}

trait SerialAnalyserControllerObj[T <: SerialController with CorpusController] {
  def className: String
  def apply(
    gateHomePath: String,
    name: String,
    outputType: SesameOutputType.SesameOutputType
  ): SesameController[T] = {
    SesameController(
      Some(gateHomePath),
      None,
      className,
      List(), List(),
      outputType, None, Some(name)
    )
  }

  def apply(
    gateHomePath: String,
    outputType: SesameOutputType.SesameOutputType
  ): SesameController[T] = {
    SesameController(
      Some(gateHomePath), None,
      className, List(), List(),
      outputType
    )
  }

  def apply(
    outputType: SesameOutputType.SesameOutputType
  ): SesameController[T] = {
    SesameController(
      None, None, className,
      List(), List(),
      outputType
    )
  }
}

class SesameException(
  detailedMessage: String = null,
  cause: Throwable = null
) extends Exception(detailedMessage, cause)

object SesameOutputType extends Enumeration {
  type SesameOutputType = Value
  val Xml, Doc, Annotations = Value
}
