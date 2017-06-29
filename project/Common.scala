import sbt._
import Keys._
import java.nio.file.Paths

object Common {
  val classPathRoot = Paths.get(sys.props("user.dir"), "target", "scala-2.11", "classes")
  val commonSettings = Seq(
    scalaVersion  := "2.11.7",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    ivyScala      := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    resolvers in ThisBuild ++= Seq(
      "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
      Resolver.jcenterRepo,
      Resolver.bintrayRepo("liuhongchao", "maven")
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging-slf4j"   % "2.1.2",
      "org.slf4j"                  % "log4j-over-slf4j"       % "1.7.21",
      "ch.qos.logback"             % "logback-classic"        % "1.1.7"
    ),
    parallelExecution in Test := false
  )

  def logging(s: String) = println(s)
}
