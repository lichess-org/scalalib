import sbt._, Keys._

object ScalalibBuild extends Build {

  lazy val core = Project("core", file(".")) settings (
    organization := "com.github.ornicar",
    name := "scalalib",
    version := "4.24",
    scalaVersion := "2.10.3",
    resolvers ++= Seq(
      "sonatype" at "http://oss.sonatype.org/content/repositories/releases"),
      libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-core" % "7.0.4",
        "org.specs2" %% "specs2" % "2.3.4"),
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
