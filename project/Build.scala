import sbt._, Keys._

object ScalalibBuild extends Build {

  lazy val core = Project("core", file(".")) settings (
    organization := "com.github.ornicar",
    name := "scalalib",
    version := "5.4",
    scalaVersion := "2.11.7",
    resolvers ++= Seq(
      "sonatype" at "http://oss.sonatype.org/content/repositories/releases",
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
    ),
      libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-core" % "7.1.1",
        "org.specs2" %% "specs2-core" % "3.0.1"),
        scalacOptions := Seq(
          "-deprecation",
          "-unchecked",
          "-feature",
          "-language:_")
  )
}
