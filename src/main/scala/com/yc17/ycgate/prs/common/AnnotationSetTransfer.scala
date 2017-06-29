package com.yc17.ycgate.prs.common

import com.yc17.ycgate.sesame._
import java.lang.Boolean._

object AnnotationSetTransfer {
  private val className = "gate.creole.annotransfer.AnnotationSetTransfer"
  private def resourceName: String = "Annotation Set Transfer"
  private val ORIGINAL_MARKUPS = "Original markups"
  def apply() = {
    SesameProcessingResource(
      "gate.creole.annotransfer.AnnotationSetTransfer",
      resourceName,
      Map(
        "transferAllUnlessFound" -> TRUE,
        "tagASName"              -> ORIGINAL_MARKUPS,
        "copyAnnotations"        -> TRUE,
        "inputASName"            -> ORIGINAL_MARKUPS
      )
    )
  }
}
