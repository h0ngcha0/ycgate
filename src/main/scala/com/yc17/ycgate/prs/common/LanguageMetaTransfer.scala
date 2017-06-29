package com.yc17.ycgate.prs.common

import com.yc17.ycgate.sesame.SesameProcessingResource
import java.lang.Boolean._
import java.util.Arrays

object LanguageMetaTransfer {
  val className = "gate.creole.annotransfer.AnnotationSetTransfer"
  val ORIGINAL_MARKUPS = "Original markups"

  val annotationTypes = Arrays.asList("meta", "html")
  // could be made more generic
  def apply() = {
    SesameProcessingResource (
      className,
      "Lang Meta Transfer",
      Map(
        "transferAllUnlessFound" -> FALSE,
        "tagASName"              -> ORIGINAL_MARKUPS,
        "copyAnnotations"        -> TRUE,
        "inputASName"            -> ORIGINAL_MARKUPS,
        "outputASName"           -> "",
        "annotationTypes"        -> annotationTypes
      )
    )
  }
}
