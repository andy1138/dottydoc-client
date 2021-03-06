import sbt._
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, Elem => XmlElem}
import scala.xml.transform.{RewriteRule, RuleTransformer}

val dottyVersion = "0.1-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-unused-import",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xlint"
)

lazy val `dottydoc-client` =
  project.in(file("."))
  .settings(
    triggeredMessage  in ThisBuild := Watched.clearWhenTriggered,
    scalaSource       in Test      := baseDirectory.value / "test",
    resourceDirectory in Compile   := baseDirectory.value / "resources",
    resources         in Compile   += (fullOptJS in (`client-js`, Compile)).value.data,
    compile           in Test      <<= (compile in Test) dependsOn cloneScalaLib,

    watchSources ++= Seq(
      baseDirectory.value / "resources",
      baseDirectory.value / "js" / "src"
    ),

    resolvers += Resolver.sonatypeRepo("snapshots"),

    libraryDependencies ++= Seq(
      "ch.epfl.lamp" % "scala-library_2.11" % dottyVersion,
      "ch.epfl.lamp" % "dotty_2.11" % dottyVersion,
      "com.github.pathikrit"  %% "better-files" % "2.16.0",
      "com.gilt" %% "handlebars-scala" % "2.1.1"
    ),

    // The rest of the settings are stolen from the dotty project build and are
    // needed in order for the compiler in test to work

    scalaVersion in Global := "2.11.5",
    ivyScala := ivyScala.value.map(_.copy(overrideScalaVersion = true)),
    autoScalaLibrary := false,

    fork in run := true,
    fork in Test := true,
    parallelExecution in Test := false,

    javaOptions <++= (dependencyClasspath in Runtime, packageBin in Compile) map { (attList, bin) =>
      // put the Scala {library, reflect} in the classpath
      val path = for {
        file <- attList.map(_.data)
        path = file.getAbsolutePath
      } yield "-Xbootclasspath/p:" + path

      // dotty itself needs to be in the bootclasspath
      ("-Xbootclasspath/p:" + "dotty.jar") :: ("-Xbootclasspath/a:" + bin) :: path.toList
    }
  )
  .settings(publishing)
  .settings(
    addCommandAlias("generateArgs",        "test:runMain dotty.tools.doc.test.ArgsMain") ++
    addCommandAlias("generateArray",       "test:runMain dotty.tools.doc.test.ArrayMain") ++
    addCommandAlias("generateCollections", "test:runMain dotty.tools.doc.test.CollectionsMain")
  )

lazy val `client-js` =
  project.in(file("js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaSource in Compile := baseDirectory.value / "src",
    artifactPath in (Compile, fullOptJS) := (crossTarget in(Compile, fullOptJS)).value / "dottydoc.js",

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0" % "provided",
      "com.lihaoyi" %%% "scalatags" % "0.5.5" % "provided"
    )
  )

lazy val cloneScalaLib = taskKey[Unit]("Download the scala-scala library compatible with dotty")

cloneScalaLib := {
  "scripts/clonelib.sh" !
}

lazy val publishing = Seq(
  version              in Global := "0.1-SNAPSHOT",
  organization         in Global := "ch.epfl.lamp",
  organizationName     in Global := "LAMP/EPFL",
  organizationHomepage in Global := Some(url("http://lamp.epfl.ch")),

  crossPaths := false,

  publishArtifact := true,
  publishArtifact in Test := false,
  publishMavenStyle := true,

  isSnapshot := version.value.contains("SNAPSHOT"),

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  licenses +=
    ("BSD New",url("https://github.com/lampepfl/dotty/blob/master/LICENSE.md")),

  scmInfo := Some(
    ScmInfo(
      url("https://github.com/lampepfl/dotty"),
      "scm:git:git@github.com:lampepfl/dotty.git"
    )
  ),

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
