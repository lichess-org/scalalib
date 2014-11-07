import sbt._, Keys._

object ScalalibBuild extends Build {

  lazy val core = Project("core", file(".")) settings (
    organization := "com.github.ornicar",
    name := "scalalib",
    version := "5.1",
    scalaVersion := "2.11.4",
    resolvers ++= Seq(
      "sonatype" at "http://oss.sonatype.org/content/repositories/releases"),
      libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-core" % "7.1.0",
        "org.specs2" %% "specs2" % "2.4.1"),
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
