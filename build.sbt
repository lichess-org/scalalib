lazy val core = Project("core", file("."))
organization := "com.github.ornicar"
name := "scalalib"
version := "6.8"
scalaVersion := "2.13.1"
crossScalaVersions := Seq("2.11.12", "2.13.1")
licenses += "MIT" -> url("http://opensource.org/licenses/MIT")
libraryDependencies ++= Seq("org.scalaz" %% "scalaz-core" % "7.2.29")
scalacOptions := Seq(
  "-language:implicitConversions",
  "-language:postfixOps",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint:_",
  "-Ywarn-macros:after",
  "-Ywarn-unused:_",
  "-Xfatal-warnings",
  "-Xmaxerrs",
  "12",
  "-Xmaxwarns",
  "12"
)
publishTo := Some(Resolver.file("file", new File(sys.props.getOrElse("publishTo", ""))))
