import play.PlayScala
import com.tuplejump.sbt.yeoman.Yeoman

name := "session-montpellier"

version := "1.0"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "2.0",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "jquery" % "1.11.0",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
  "com.typesafe.play" %% "play-slick" % "0.8.0",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc4",
  "com.github.tminglei" %% "slick-pg" % "0.8.5",
  cache,
  ws
)

Yeoman.yeomanSettings

lazy val root = (project in file(".")).enablePlugins(PlayScala)
