lazy val scalalib = Project("scalalib", file("."))
organization                           := "com.github.ornicar"
name                                   := "scalalib"
version                                := "9.4.0"
scalaVersion                           := "3.3.0-RC6"
licenses += "MIT"                      -> url("https://opensource.org/licenses/MIT")
libraryDependencies += "org.typelevel" %% "cats-core"      % "2.9.0"
libraryDependencies += "org.typelevel" %% "alleycats-core" % "2.9.0"
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
  "-Xtarget:12",
  "-Wunused:all"
)
publishTo := Some(Resolver.file("file", new File(sys.props.getOrElse("publishTo", ""))))
