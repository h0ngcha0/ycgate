package com.yc17.ycgate.sesame.utils

import gate.LanguageAnalyser
import gate.Controller
import com.typesafe.config.ConfigFactory
import scalaz._
import Scalaz._

import scala.collection.JavaConversions._

object GateUtils {
  def prettyPrintJapeAndGazeteer(controller: Controller): Unit = {
    println("### start ###")
    constructJapeAndGazeteerTree(controller).map(prettyPrintTree(_))
    println("### done! ###")
  }

  def prettyPrintPRs(controller: Controller): Unit = {
    println("### start ###")
    constructPRTree(controller).map(prettyPrintTree(_))
    println("### done! ###")
  }

  def constructJapeTree(gateConstruct: Any): Option[Tree[String]] =
    constructTree(gateConstruct, findJapeURL _)

  def constructGazeteerTree(gateConstruct: Any): Option[Tree[String]] = {
    constructTree(gateConstruct, findGazeteerURL _)
  }

  def constructPRTree(gateConstruct: Any): Option[Tree[String]] =
    constructTree(gateConstruct, findResourceTypeAndName _)

  def constructJapeAndGazeteerTree(gateConstruct: Any): Option[Tree[String]] =
    constructTree(gateConstruct, (la: LanguageAnalyser) => {
      findJapeURL(la).orElse(findGazeteerURL(la))
    })

  def constructTree(
    gateConstruct: Any,
    findFun: (LanguageAnalyser) => Option[String]
  ): Option[Tree[String]] = gateConstruct match {
    case controller: Controller =>
      val subForest = controller.getPRs.map { constructTree(_, findFun) }.filter(!_.isEmpty).map(_.get)
      (!subForest.isEmpty).option(
        Tree.node(controller.getName, subForest.toStream).some
      ).getOrElse(None)
    case analyser: LanguageAnalyser =>
      findFun(analyser).map(_.leaf)
  }

  def prettyPrintTree(tree: Tree[String], depth: Int = 1): Unit = {
    println(s"""${"."*depth} ${tree.rootLabel}""")
    tree.subForest.foreach(prettyPrintTree(_, depth+1))
  }

  def findResourceTypeAndName(la: LanguageAnalyser): Option[String] = {
    val desc = findJapeURL(la).orElse(findGazeteerURL(la)).getOrElse(la.getClass.getName)
    (la.getName + s" ($desc)").some
  }

  def findJapeURL(la: LanguageAnalyser): Option[String] = {
    scala.util.Try {
      la.getParameterValue("grammarURL").toString
    } match {
      case scala.util.Success(v) => v.endsWith(".jape").option(v)
      case scala.util.Failure(_) => none
    }
  }

  def findGazeteerURL(la: LanguageAnalyser): Option[String] = {
    scala.util.Try {
      la.getParameterValue("listsURL").toString
    } match {
      case scala.util.Success(v) => (v.endsWith(".def") || v.endsWith(".lst")).option(v)
      case scala.util.Failure(_) => None
    }
  }
}
