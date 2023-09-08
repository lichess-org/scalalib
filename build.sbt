lazy val scalalib = Project("scalalib", file("."))
organization                           := "com.github.ornicar"
name                                   := "scalalib"
version                                := "9.5.5"
scalaVersion                           := "3.3.1"
licenses += "MIT"                      -> url("https://opensource.org/licenses/MIT")
libraryDependencies += "org.typelevel" %% "cats-core"      % "2.10.0"
libraryDependencies += "org.typelevel" %% "alleycats-core" % "2.10.0"
libraryDependencies += "com.lihaoyi"   %% "pprint"         % "0.7.0"
scalacOptions := Seq(
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:postfixOps",
  "-indent",
  "-rewrite",
  "-source:future-migration",
  "-release:17",
  "-Wunused:all"
)
publishTo := Some(Resolver.file("file", new File(sys.props.getOrElse("publishTo", ""))))
