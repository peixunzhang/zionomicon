import sbt._

object Dependencies {
  val Worksheets = Seq(
    "org.typelevel" %% "cats-core" % "2.8.0",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    "dev.zio" %% "zio" % "2.0.15"
  )
}
