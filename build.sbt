import play.sbt.PlaySettings
import sbt.Keys._

lazy val GatlingTest = config("gatling") extend Test

scalacOptions += "-Ypartial-unification"


scalaVersion in ThisBuild := "2.12.7"

libraryDependencies += guice
libraryDependencies += "org.joda" % "joda-convert" % "2.1.2"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.2"

libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.16"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.1"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.1.1" % Test
libraryDependencies += "io.gatling" % "gatling-test-framework" % "3.0.1.1" % Test
libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.2.0"
libraryDependencies += "org.typelevel" %% "cats-mtl-core" % "0.5.0"
libraryDependencies += "org.scanamo" %% "scanamo" % "1.0.0-M9"
//libraryDependencies += "com.gu" %% "scanamo-alpakka" % "1.0.0-M8"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.537"
libraryDependencies += "org.mockito" % "mockito-core" % "2.27.0" % Test


// The Play project itself
lazy val root = (project in file("."))
  .enablePlugins(Common, PlayService, PlayLayoutPlugin, GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)
  .settings(
    name := """play-scala-rest-api-example""",
    scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation"
  )

// Documentation for this project:
//    sbt "project docs" "~ paradox"
//    open docs/target/paradox/site/index.html
lazy val docs = (project in file("docs")).enablePlugins(ParadoxPlugin).
  settings(
    paradoxProperties += ("download_url" -> "https://example.lightbend.com/v1/download/play-rest-api")
  )
