// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Sonatype" at "http://oss.sonatype.org/content/repositories/releases"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"


// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("play.version"))