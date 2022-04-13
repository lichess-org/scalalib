lazy val scalalib = Project("scalalib", file("."))
organization := "com.github.ornicar"
name := "scalalib"
version := "8.0.0"
scalaVersion := "3.1.1"
licenses += "MIT" -> url("http://opensource.org/licenses/MIT")
libraryDependencies += "org.typelevel" %% "cats-core" % "2.7.0"
libraryDependencies += "org.typelevel" %% "alleycats-core" % "2.7.0"
scalacOptions := Seq(
    "-encoding",
    "utf-8",
    "-explaintypes",
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    // Warnings as errors!
    // "-Xfatal-warnings",
    // Linting options
    "-unchecked",
    // "-Wnumeric-widen",
)
publishTo := Some(Resolver.file("file", new File(sys.props.getOrElse("publishTo", ""))))
