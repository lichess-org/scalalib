lazy val scalalib = Project("scalalib", file("."))
organization := "com.github.ornicar"
name         := "scalalib"
version      := "8.1.1"
scalaVersion := "3.2.1"
// crossScalaVersions ++= Seq("2.13.8", "3.1.3")
licenses += "MIT"                      -> url("https://opensource.org/licenses/MIT")
libraryDependencies += "org.typelevel" %% "cats-core"      % "2.8.0"
libraryDependencies += "org.typelevel" %% "alleycats-core" % "2.8.0"
scalacOptions := Seq(
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:postfixOps",
  "-rewrite",
  "-indent",
  "-source:future-migration"
)
publishTo := Some(Resolver.file("file", new File(sys.props.getOrElse("publishTo", ""))))
