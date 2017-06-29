package com.yc17.ycgate.sesame

import gate.FeatureMap

case class SesameAnnotation(
  id: Int, startOffset: Int, endOffset: Int, name: String, features: FeatureMap
)
