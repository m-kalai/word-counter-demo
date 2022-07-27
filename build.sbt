ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "cz.kalai"

lazy val root = (project in file("."))
  .settings(
    name := "word-counter-demo",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"            % Versions.zio,
      "dev.zio" %% "zio-concurrent" % Versions.zio,
      "dev.zio" %% "zio-test"       % Versions.zio % Test,
      "dev.zio" %% "zio-test-sbt"   % Versions.zio % Test,
      "dev.zio" %% "zio-process"    % "0.7.1",
      "io.d11"  %% "zhttp"          % "2.0.0-RC10",
      "dev.zio" %% "zio-json"       % "0.3.0-RC10"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
