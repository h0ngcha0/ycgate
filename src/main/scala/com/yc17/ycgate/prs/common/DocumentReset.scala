package com.yc17.ycgate.prs.common

import com.yc17.ycgate.sesame.SesameProcessingResource
import java.lang.Boolean.TRUE

object DocumentReset {
  private val className = "gate.creole.annotdelete.AnnotationDeletePR"
  private def resourceName = "Document Reset"

  def apply() = {
    SesameProcessingResource(
      className,
      resourceName,
      Map(
        "keepOriginalMarkupsAS" -> TRUE
      ),
      Map()
    )
  }
}
