import sbt._, Keys._

object ScalalibBuild extends Build {

  lazy val core = Project("core", file(".")) settings (
    organization := "com.github.ornicar",
    name := "scalalib",
    version := "6.1",
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.11.11", "2.12.2"),
    licenses += "MIT" -> url("http://opensource.org/licenses/MIT"),
    resolvers ++= Seq(
      "sonatype" at "http://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.2.13",
      "org.specs2" %% "specs2-core" % "3.9.0"),
    scalacOptions := Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-language:_",
      "-Xfatal-warnings"),
    publishTo := Some(Resolver.file("file",  new File(sys.props.getOrElse("publishTo", ""))))
  )
}
