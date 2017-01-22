enablePlugins(ScalaJSPlugin)

name := "MeritJS"
version := "0.0.1"
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.singlespaced" %%% "scalajs-d3" % "0.3.4"
)

persistLauncher := true
