val reactJS      = "16.13.1"
val scalaJsReact = "1.7.7"
val reactSizeMe  = "2.6.12"

parallelExecution in (ThisBuild, Test) := false

cancelable in Global := true

resolvers in Global += Resolver.sonatypeRepo("public")

addCommandAlias(
  "restartWDS",
  "; demo/fastOptJS::stopWebpackDevServer; demo/fastOptJS::startWebpackDevServer; ~demo/fastOptJS"
)

ThisBuild / turbo := true

Global / onChangedBuildSource := ReloadOnSourceChanges

// sbt-release-early
inThisBuild(
  List(
    homepage := Some(url("https://github.com/cquiroz/scalajs-react-sizeme")),
    licenses := Seq("BSD 3-Clause License" -> url("https://opensource.org/licenses/BSD-3-Clause")),
    developers := List(
      Developer("cquiroz",
                "Carlos Quiroz",
                "carlos.m.quiroz@gmail.com",
                url("https://github.com/cquiroz")
      )
    ),
    scmInfo := Some(
      ScmInfo(url("https://github.com/cquiroz/scalajs-react-sizeme"),
              "scm:git:git@github.com:cquiroz/scalajs-react-sizeme.git"
      )
    )
  )
)

val root =
  project
    .in(file("."))
    .settings(commonSettings: _*)
    .aggregate(facade, demo)
    .settings(
      name := "root",
      // No, SBT, we don't want any artifacts for root.
      // No, not even an empty jar.
      publish := {},
      publishLocal := {},
      publishArtifact := false,
      Keys.`package` := file("")
    )

lazy val demo =
  project
    .in(file("demo"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(commonSettings: _*)
    .settings(
      version in webpack := "4.32.0",
      version in startWebpackDevServer := "3.3.1",
      webpackConfigFile in fastOptJS := Some(
        baseDirectory.value / "webpack" / "dev.webpack.config.js"
      ),
      webpackConfigFile in fullOptJS := Some(
        baseDirectory.value / "webpack" / "prod.webpack.config.js"
      ),
      webpackMonitoredDirectories += (resourceDirectory in Compile).value,
      webpackResources := (baseDirectory.value / "webpack") * "*.js",
      includeFilter in webpackMonitoredFiles := "*",
      webpackExtraArgs := Seq("--progress"),
      // webpackExtraArgs                       := Seq("--progress", "--display", "verbose"),
      useYarn := true,
      webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
      webpackBundlingMode in fullOptJS := BundlingMode.Application,
      test := {},
      // NPM libs for development, mostly to let webpack do its magic
      npmDevDependencies in Compile ++= Seq(
        "postcss-loader"                     -> "3.0.0",
        "autoprefixer"                       -> "9.4.4",
        "url-loader"                         -> "1.1.1",
        "file-loader"                        -> "3.0.1",
        "css-loader"                         -> "2.1.0",
        "style-loader"                       -> "0.23.1",
        "less"                               -> "3.9.0",
        "less-loader"                        -> "4.1.0",
        "webpack-merge"                      -> "4.2.1",
        "mini-css-extract-plugin"            -> "0.5.0",
        "webpack-dev-server-status-bar"      -> "1.1.0",
        "cssnano"                            -> "4.1.8",
        "uglifyjs-webpack-plugin"            -> "2.1.1",
        "html-webpack-plugin"                -> "3.2.0",
        "optimize-css-assets-webpack-plugin" -> "5.0.1",
        "favicons-webpack-plugin"            -> "0.0.9",
        "why-did-you-update"                 -> "1.0.6"
      ),
      npmDependencies in Compile ++= Seq(
        "react"        -> reactJS,
        "react-dom"    -> reactJS,
        "react-sizeme" -> reactSizeMe
      ),
      // don't publish the demo
      publish := {},
      publishLocal := {},
      publishArtifact := false,
      Keys.`package` := file("")
    )
    .dependsOn(facade)

lazy val facade =
  project
    .in(file("facade"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "react-sizeme",
      version in webpack := "4.32.0",
      version in startWebpackDevServer := "3.3.1",
      // Requires the DOM for tests
      requireJsDomEnv in Test := true,
      // Compile tests to JS using fast-optimisation
      // scalaJSStage in Test            := FastOptStage,
      npmDependencies in Compile ++= Seq(
        "react"        -> reactJS,
        "react-dom"    -> reactJS,
        "react-sizeme" -> reactSizeMe
      ),
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalajs-react" %%% "core"   % scalaJsReact,
        "com.github.japgolly.scalajs-react" %%% "test"   % scalaJsReact % Test,
        "io.github.cquiroz.react"           %%% "common" % "0.11.3",
        "com.lihaoyi"                       %%% "utest"  % "0.7.7"      % Test
      ),
      webpackConfigFile in Test := Some(baseDirectory.value / "webpack" / "test.webpack.config.js"),
      testFrameworks += new TestFramework("utest.runner.Framework")
    )

lazy val commonSettings = Seq(
  scalaVersion := "2.13.5",
  organization := "io.github.cquiroz.react",
  sonatypeProfileName := "io.github.cquiroz",
  description := "scala.js facade for react-sizeme",
  publishArtifact in Test := false,
  scalacOptions ~= (_.filterNot(
    Set(
      // By necessity facades will have unused params
      "-Wdead-code",
      "-Wunused:params"
    )
  ))
)
