package com.yc17.ycgate.pipelines

import gate.creole.ConditionalSerialAnalyserController
import com.yc17.ycgate.sesame._

trait Pipeline {
  def plugins: List[String]                 // a list of plugin names
  def prs: List[SesameProcessingResource]   // a list of processing resources
  def apply(): SesameController[ConditionalSerialAnalyserController] // conditional controller
}
