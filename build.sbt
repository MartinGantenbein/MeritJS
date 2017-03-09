enablePlugins(ScalaJSPlugin)

name := "MeritJS"
version := "0.4.0"
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.singlespaced" %%% "scalajs-d3" % "0.3.4"
)

// workaround for chord bug in 0.3.4
//unmanagedClasspath in Compile += baseDirectory.value.getParentFile / "scala-js-d3/target/scala-2.12/classes"
//jsDependencies += "org.webjars" % "d3js" % "3.5.17" / "3.5.17/d3.min.js"
// end of workaround

persistLauncher := true
