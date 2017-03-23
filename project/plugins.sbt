logLevel := Level.Warn

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.Darkyenus" % "ResourcePacker" % "1.8"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

addSbtPlugin("com.hanhuy.sbt" % "kotlin-plugin" % "1.0.6")
