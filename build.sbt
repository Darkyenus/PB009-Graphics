
import com.darkyen.resourcepacker.PackingOperation

import scala.collection.mutable.ArrayBuffer

name := "PB009"

version := "0.1-SNAPSHOT"

organization := "com.darkyenus"

crossPaths := false

autoScalaLibrary := false

kotlinVersion := "1.1.1"

kotlinLib("stdlib")

val gdxVersion = "1.9.6"

baseDirectory in (Compile, run) := baseDirectory.value / "assets"

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
  ResourcePacker.resourcePack(new PackingOperation("./resources", "./assets"))
}

val MainClass = "com.darkyen.pb009.MainKt"

mainClass in assembly := Some(MainClass)

mainClass in (Compile, run) := Some(MainClass)

mainClass := Some(MainClass)

TaskKey[Unit]("dist") := {
  packResources.value
  //
  val resultZip = target.value / (name.value+"-"+version.value+".zip")
  val basePrefix = "PB009/"
//
  val files = new ArrayBuffer[(File, String)]()
  files += ((assembly.value, basePrefix + "PB009.jar"))
//
  def appendContent(directory:File, prefix:String): Unit ={
    for(file <- directory.listFiles() if !file.getName.startsWith(".")) {
      if(file.isFile){
        files += ((file, prefix+file.getName))
      }else if(file.isDirectory){
        appendContent(file, s"$prefix${file.getName}/")
      }
    }
  }
//
  appendContent((baseDirectory in(Compile, run)).value, basePrefix)
//
  IO.zip(files, resultZip)
  println("Packed to "+resultZip.getCanonicalPath)
}
