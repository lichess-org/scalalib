import sbt._
import Keys._

object ScalalibBuild extends Build {

  lazy val core = Project("core", file(".")) settings (
    organization := "com.github.ornicar",
    name := "scalalib",
    version := "4.4",
    scalaVersion := "2.10.2",
    resolvers ++= Seq(
      "sonatype" at "http://oss.sonatype.org/content/repositories/releases"),
      libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-core" % "7.0.3",
        "org.specs2" %% "specs2" % "1.14",
        "joda-time" % "joda-time" % "2.1",
        "org.joda" % "joda-convert" % "1.2"
      ),
        scalacOptions := Seq(
          "-deprecation",
          "-unchecked",
          "-feature",
          "-language:_"),
          publishTo := Some(Resolver.sftp(
            "iliaz",
            "scala.iliaz.com"
          ) as ("scala_iliaz_com", Path.userHome / ".ssh" / "id_rsa"))
  )
}
