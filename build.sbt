name := """tplay"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc"  % "3.5.0"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-config" % "3.5.0"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "2.8.0-scalikejdbc-3.5"
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.29"
libraryDependencies += ehcache
libraryDependencies += jdbc % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
