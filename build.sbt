inThisBuild(
  Seq(
    scalaVersion      := "3.4.1",
    versionScheme     := Some("early-semver"),
    version           := "10.0.5",
    organization      := "com.github.lichess-org",
    licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
  )
)

val commonSettings = Seq(
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
)

lazy val core: Project = Project("core", file("core")).settings(
  commonSettings,
  name := "core",
  libraryDependencies ++= List(
    "org.typelevel"                %% "cats-core"      % "2.10.0",
    "org.typelevel"                %% "alleycats-core" % "2.10.0",
    "com.lihaoyi"                  %% "pprint"         % "0.7.0",
    "com.github.ben-manes.caffeine" % "caffeine"       % "3.1.8"     % "compile",
    "com.github.blemale"           %% "scaffeine"      % "5.2.1"     % "compile",
    "org.scalameta"                %% "munit"          % "1.0.0-M11" % Test
  )
)

lazy val root = project
  .in(file("."))
  .settings(publish := {}, publish / skip := true)
  .aggregate(core)
