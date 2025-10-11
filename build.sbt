inThisBuild(
  Seq(
    scalaVersion := "3.7.3",
    versionScheme := Some("early-semver"),
    version := "11.9.4",
    organization := "org.lichess",
    licenses += ("MIT" -> url("https://opensource.org/licenses/MIT")),
    publishTo := Option(Resolver.file("file", new File(sys.props.getOrElse("publishTo", ""))))
  )
)

val commonSettings = Seq(
  javacOptions ++= Seq("--release", "21"),
  scalacOptions := Seq(
    "-encoding",
    "utf-8",
    "-explaintypes",
    "-feature",
    "-language:postfixOps",
    "-indent",
    "-rewrite",
    // "-source:future-migration",
    "-release:21",
    "-Wunused:all"
  )
)

lazy val core: Project = Project("core", file("core")).settings(
  commonSettings,
  name := "scalalib-core",
  libraryDependencies ++= List(
    "org.typelevel" %% "cats-core" % "2.13.0",
    "org.typelevel" %% "alleycats-core" % "2.13.0",
    "com.lihaoyi" %% "pprint" % "0.9.4"
  )
)

lazy val model: Project = Project("model", file("model"))
  .settings(
    commonSettings,
    name := "scalalib-model"
  )
  .dependsOn(core)

lazy val playJson: Project = Project("playJson", file("playJson"))
  .settings(
    commonSettings,
    name := "scalalib-play-json",
    libraryDependencies ++= List(
      "org.playframework" %% "play-json" % "3.0.5"
    )
  )
  .dependsOn(core)

// functions that useful for lila & lila-ws
lazy val lila: Project = Project("lila", file("lila"))
  .settings(
    commonSettings,
    name := "scalalib-lila",
    libraryDependencies ++= List(
      "com.github.ben-manes.caffeine" % "caffeine" % "3.2.2" % "compile",
      "com.github.blemale" %% "scaffeine" % "5.3.0" % "compile",
      "org.scalameta" %% "munit" % "1.2.0" % Test,
      "com.github.lichess-org" % "TypeMap" % "0.2.2"
    ),
    resolvers += "jitpack".at("https://jitpack.io")
  )
  .dependsOn(core, model, playJson)

lazy val root = project
  .in(file("."))
  .settings(publish := {}, publish / skip := true)
  .aggregate(core, model, playJson, lila)
