lazy val core = Project("core", file("."))
organization := "com.github.ornicar"
name := "scalalib"
version := "6.6"
scalaVersion := "2.12.6"
crossScalaVersions := Seq("2.11.12", "2.12.6")
licenses += "MIT" -> url("http://opensource.org/licenses/MIT")
resolvers ++= Seq(
  "sonatype" at "http://oss.sonatype.org/content/repositories/releases"
)
libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.23")
scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:_",
  "-Xfatal-warnings")
publishTo := Some(Resolver.file("file",  new File(sys.props.getOrElse("publishTo", ""))))
