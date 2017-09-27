import Dependencies._
import com.typesafe.sbt.packager.docker._



name := "opendata-dekamer"
organization := "consulting.vectr"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.11.8"
startYear := Some(2017)
resolvers ++= Dependencies.resolutionRepos
licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
maintainer in Docker := "Tom Michiels <tom.michiels@vectr.consulting>"
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      // setting the run script executable
      ExecCmd("RUN",
        "usermod", "-d", "/opt/docker", "daemon"),
      // setting a daemon user
      Cmd("USER", "daemon")
    )
libraryDependencies ++= dependencies ++ finatraDependencies
libraryDependencies ++=  compileStage(neo4jDriver)
libraryDependencies ++=  compileStageWithClassifier(neo4jKernel)
libraryDependencies ++=  compileStageWithClassifier(neo4jIO)
libraryDependencies ++=  compileStage(neo4jTestHarness)
libraryDependencies ++=  compileStage(jerseyCore)
libraryDependencies ++=  logbackElastic
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "1.0.2"
libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6"
