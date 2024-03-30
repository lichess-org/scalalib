lazy val scalalib = Project("scalalib", file("."))
organization                                          := "com.github.lichess-org"
name                                                  := "scalalib"
version                                               := "10.0.1"
scalaVersion                                          := "3.4.1"
licenses += "MIT"                                     -> url("https://opensource.org/licenses/MIT")
libraryDependencies += "org.typelevel"                %% "cats-core"      % "2.10.0"
libraryDependencies += "org.typelevel"                %% "alleycats-core" % "2.10.0"
libraryDependencies += "com.lihaoyi"                  %% "pprint"         % "0.7.0"
libraryDependencies += "com.github.ben-manes.caffeine" % "caffeine"       % "3.1.8"     % "compile"
libraryDependencies += "com.github.blemale"           %% "scaffeine"      % "5.2.1"     % "compile"
libraryDependencies += "org.scalameta"                %% "munit"          % "1.0.0-M11" % Test

scalacOptions := Seq(
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:postfixOps",
  "-indent",
  "-rewrite",
  "-source:future-migration",
  "-release:21",
  "-Wunused:all"
)
publishTo := Some(Resolver.file("file", new File(sys.props.getOrElse("publishTo", ""))))
