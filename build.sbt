name := "twstyles"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe" % "config" % "1.3.0"
)

mainClass in (Compile, run) := Some("twstyles.Boot")