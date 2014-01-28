name := "datagen"

version := "1.0-SNAPSHOT"

//conflictManager := ConflictManager.strict

libraryDependencies ++= Seq(
"joda-time"         % "joda-time"           % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.typesafe" % "config" % "1.0.2",
  "org.streum" %% "configrity-core" % "1.0.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2",
  "org.apache.cassandra" % "cassandra-all" % "2.0.4",
  //"org.apache.cassandra" % "cassandra-thrift" % "2.0.2",
  "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.2",
  "org.codehaus.jackson" % "jackson-core-asl" % "1.9.2",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.0-rc2",
  "org.xerial.snappy" % "snappy-java" % "1.1.0",
  "org.mongodb" %% "casbah" % "2.6.4",
  "org.elasticsearch" % "elasticsearch" % "0.90.9",
  jdbc,
  anorm,
  cache  
)

dependencyOverrides += "org.scala-lang" % "scala-library" % "2.10.2"

dependencyOverrides += "org.scala-lang" % "scala-reflect" % "2.10.2"


dependencyOverrides += "com.google.guava" % "guava" % "15.0"

dependencyOverrides += "io.netty" % "netty" % "3.7.0.Final"

dependencyOverrides += "org.codehaus.jackson" % "jackson-core-asl" % "1.9.2"

dependencyOverrides += "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.2"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.5"

dependencyOverrides += "org.xerial.snappy" % "snappy-java" % "1.1.0"

dependencyOverrides += "joda-time"         % "joda-time"           % "2.3"

dependencyOverrides += "org.joda" % "joda-convert" % "1.5"

dependencyOverrides += "org.antlr" % "antlr-runtime" % "3.5"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.2.2"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.3"
 
play.Project.playScalaSettings
