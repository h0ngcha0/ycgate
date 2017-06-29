import sbt._
import Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import Common._

object YCGateBuild extends Build {
  val scalazVersion = "7.1.3"
  lazy val ycgate = Project(
    "yc-gate",
    file(".")
  ).enablePlugins(JavaAppPackaging).settings(commonSettings).settings(
    organization := "com.yc17",
    version := "1.1.1",
    libraryDependencies ++= Seq(
      ("uk.ac.gate" % "gate-core" % "8.1").
        exclude("log4j", "log4j").
        exclude("org.apache.xmlbeans", "xmlbeans"),
      "org.scalaz"       %% "scalaz-core"   % scalazVersion,
      "org.scalaz"       %% "scalaz-effect" % scalazVersion,
      "com.github.scopt" %% "scopt"         % "3.3.0",
      "com.typesafe"     %  "config"        % "1.3.1",
      "org.pegdown"      % "pegdown"        % "1.6.0",
      "com.lihaoyi"      %  "ammonite-repl" % "0.5.6"         % "test" cross CrossVersion.full,
      "org.scalatest"    %% "scalatest"     % "2.2.4"         % "test"
    ),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/htmlreport"),
    initialCommands in (Test, console) := """ammonite.repl.Main.run("")""",
    parallelExecution in Test := false,
    setupGatePlugins := (Def.task {
      logging("\n* Gate plugins are successfully setup! *\n")
    }
      .dependsOn(publish in gate)
    ).value
  )

  lazy val gate = Gate.project
  lazy val setupGatePlugins = taskKey[Unit]("Task that sets up all the gate plugins")
}
