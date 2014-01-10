import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "datagen"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
  "joda-time"         % "joda-time"           % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.typesafe" % "config" % "1.0.2",
  "org.streum" %% "configrity-core" % "1.0.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.1.3",
  "org.elasticsearch" % "elasticsearch" % "0.90.9"

  )
  
  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}