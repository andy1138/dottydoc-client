import sbt._
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, Elem => XmlElem}
import scala.xml.transform.{RewriteRule, RuleTransformer}

scalaVersion := "2.11.8"

lazy val `dottydoc-client` = project.in(file("."))
  .settings(
    resourceDirectory in Compile := baseDirectory.value / "resources",
    resources in Compile += (fullOptJS in (`client-js`, Compile)).value.data
  )
  .settings(publishing)


lazy val `client-js` = project.in(file("js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    triggeredMessage  in ThisBuild := Watched.clearWhenTriggered,
    scalaSource       in Compile   := baseDirectory.value / "src",

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0" % "provided",
      "com.lihaoyi" %%% "scalatags" % "0.5.5" % "provided"
    )
  )

lazy val publishing = Seq(
  version              in Global := "0.1-SNAPSHOT",
  organization         in Global := "ch.epfl.lamp",
  organizationName     in Global := "LAMP/EPFL",
  organizationHomepage in Global := Some(url("http://lamp.epfl.ch")),

  crossPaths := false,

  publishArtifact in Test := false,

  licenses += ("BSD New",
    url("https://github.com/lampepfl/dotty/blob/master/LICENSE.md")),

  pomExtra := (
    <developers>
      <developer>
        <id>felixmulder</id>
        <name>Felix Mulder</name>
        <email>felix.mulder@gmail.com</email>
        <url>http://felixmulder.com</url>
      </developer>
    </developers>
  ),

  pomPostProcess := { node =>
    // Remove all dependencies since we're only distributing a compiled copy as
    // a resource
    val removeDeps = new RewriteRule {
      override def transform(n: XmlNode): XmlNodeSeq = n match {
        case e: XmlElem if e != null && e.label == "dependencies" =>
          <dependencies>
          </dependencies>
        case _ => n
      }
    }

    new RuleTransformer(removeDeps).transform(node).head
  }
)
