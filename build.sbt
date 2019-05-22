val ZioVersion     = "1.0-RC4"
val Http4sVersion  = "0.20.0"
val CirceVersion   = "0.11.1"
val Specs2Version  = "4.1.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "com.avast.zioworkshop",
    name := "zio-workshop",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.scalaz"     %% "scalaz-zio"              % ZioVersion,
      "org.scalaz"     %% "scalaz-zio-interop-cats" % ZioVersion,
      "org.http4s"     %% "http4s-blaze-server"     % Http4sVersion,
      "org.http4s"     %% "http4s-blaze-client"     % Http4sVersion,
      "org.http4s"     %% "http4s-circe"            % Http4sVersion,
      "org.http4s"     %% "http4s-dsl"              % Http4sVersion,
      "io.circe"       %% "circe-generic"           % CirceVersion,
      "org.specs2"     %% "specs2-core"             % Specs2Version % "test",
      "ch.qos.logback" % "logback-classic"          % LogbackVersion,
      "postgresql"     % "postgresql"               % "9.1-901-1.jdbc4"
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.6"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4"),
    // addCompilerPlugin("io.tryp" % "splain" % "0.4.1" cross CrossVersion.patch)
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification"
)
