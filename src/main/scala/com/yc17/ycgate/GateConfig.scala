package com.yc17.ycgate

import com.typesafe.config.ConfigFactory
import scalaz.Scalaz._
import java.nio.file.{Paths, Path}

object GateConfig {
  private val config   = ConfigFactory.load()

  // Try gate.home configuration variable, otherwise fallback to /home in classpath.
  val gateHome: Path =
    config.hasPath("gate.home") ? Paths.get(config.getString("gate.home")) |
                                  Paths.get(this.getClass.getResource("/home/").getFile)

  val gazetteerHome: Path = Paths.get(gateHome.toString, "gazetteer")
  val japeHome: Path = Paths.get(gateHome.toString, "jape")
  val pluginHome: Path = Paths.get(gateHome.toString, "plugins")
}
