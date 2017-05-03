
import com.darkyen.resourcepacker.PackingOperation

import scala.collection.mutable.ArrayBuffer

name := "PB009"

version := "0.1-SNAPSHOT"

organization := "com.darkyenus"

crossPaths := false

autoScalaLibrary := false

kotlinVersion := "1.1.2"

kotlinLib("stdlib")

val gdxVersion = "1.9.6"

fork in run := true

resolvers += "jitpack" at "https://jitpack.io"

// Core
libraryDependencies ++= Seq(
  "com.badlogicgames.gdx" % "gdx" % gdxVersion,
  "com.badlogicgames.gdx" % "gdx-box2d" % gdxVersion
)

//Desktop
libraryDependencies ++= Seq(
  "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % gdxVersion,
  "com.badlogicgames.gdx" % "gdx-platform" % gdxVersion classifier "natives-desktop",
  "com.badlogicgames.gdx" % "gdx-box2d-platform" % gdxVersion classifier "natives-desktop"
)

javacOptions ++= Seq("-g", "-Xlint", "-Xlint:-rawtypes", "-Xlint:-unchecked")

javaOptions ++= (if (System.getProperty("os.name").contains("Mac")) Seq("-ea", "-XstartOnFirstThread") else Seq("-ea"))

val packResources = taskKey[Unit]("Packs application's resources")

packResources := {
  ResourcePacker.resourcePack(new PackingOperation("./resources", "./src/main/resources"))
}

val MainClass = "com.darkyen.pb009.MainKt"

mainClass in assembly := Some(MainClass)

mainClass in (Compile, run) := Some(MainClass)

mainClass := Some(MainClass)