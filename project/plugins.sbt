logLevel := Level.Warn

resolvers += "jitpack" at "https://jitpack.io"

//libraryDependencies += "com.github.Darkyenus" % "ResourcePacker_2.11" % "-SNAPSHOT"

libraryDependencies += "com.github.Darkyenus" % "resourcepacker" % "2.0-SNAPSHOT"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

addSbtPlugin("com.hanhuy.sbt" % "kotlin-plugin" % "1.0.6")
