
name := "AkkaAssignment1"

version   := 	"1.0"

scalaVersion := "2.11.4"

//remove -feature warning for postfix operator "seconds"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps")

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.3.4")

parallelExecution in Test := false

