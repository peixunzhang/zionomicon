import sbt._
import Dependencies.Versions.{zio => zio}

object Dependencies {
  object Versions {
    val zio = "2.1.9"
  }

  val Worksheets = Seq(
    "org.typelevel" %% "cats-core" % "2.8.0",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    "dev.zio" %% "zio" % zio,
    "dev.zio" %% "zio-test"          % zio % Test,
    "dev.zio" %% "zio-test-sbt"      % zio % Test,
    "dev.zio" %% "zio-test-magnolia" % zio % Test
  )
}
