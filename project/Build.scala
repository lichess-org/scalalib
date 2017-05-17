import sbt._, Keys._

object ScalalibBuild extends Build {

  lazy val core = Project("core", file(".")) settings (
    organization := "com.github.ornicar",
    name := "scalalib",
    version := "5.7",
    scalaVersion := "2.11.11",
    licenses += "MIT" -> url("http://opensource.org/licenses/MIT"),
    resolvers ++= Seq(
      "sonatype" at "http://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.1.11",
      "org.specs2" %% "specs2-core" % "3.6"),
    scalacOptions := Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-language:_",
      "-Xfatal-warnings"),
    publishTo := Some(Resolver.file("file",  new File(sys.props.getOrElse("publishTo", ""))))
  )
}
