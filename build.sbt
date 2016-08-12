import sbt._
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, Elem => XmlElem}
import scala.xml.transform.{RewriteRule, RuleTransformer}

lazy val `dottydoc-client` = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaSource       in Compile := baseDirectory.value / "src",
    resourceDirectory in Compile := baseDirectory.value / "resources",

    crossTarget in (Compile, fullOptJS) := (resourceDirectory in Compile).value,
    crossTarget in (Compile, fastOptJS) := (resourceDirectory in Compile).value,

    artifactPath in(Compile, fullOptJS) :=
      (crossTarget in(Compile, fullOptJS)).value / "dottydoc.js",

    artifactPath in(Compile, fastOptJS) :=
      (crossTarget in(Compile, fastOptJS)).value / "dottydoc-fast.js",

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0" % "provided",
      "com.lihaoyi" %%% "scalatags" % "0.5.5" % "provided"
    )
  )
  .settings(publishing)

lazy val publishing = Seq(
  organization         in Global := "ch.epfl.lamp",
  organizationName     in Global := "LAMP/EPFL",
  organizationHomepage in Global := Some(url("http://lamp.epfl.ch")),

  publishLocal <<= publishLocal dependsOn (fullOptJS in Compile),
  publish      <<= publish      dependsOn (fullOptJS in Compile),

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

scalaVersion := "2.11.8"
