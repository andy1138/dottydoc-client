package dotty.tools.doc
package test

import better.files._
import java.io.{File => JFile}

import dotty.tools.dottydoc.api.scala.Dottydoc

object GenerateCollections extends Dottydoc {
  def getResource(path: String): File =
    new JFile(getClass.getResource(path).toURI).toScala

  val templatePath: String =
    getResource("/template.html").path.toAbsolutePath.toString

  val whitelistedFiles: Array[String] =
    getResource("/scala-collections.whitelist")
    .lines
    .map(_.trim)
    .foldLeft(Array.empty[String]) { (acc, line) =>
      if (line.startsWith("#") || line.isEmpty || line.endsWith("package.scala")) acc
      else acc :+ line
    }

  val resources: List[String] =
    getResource("/template.html")
    .parent
    .list
    .map(_.path.toAbsolutePath.toString)
    .toList

  override def main(args: Array[String]): Unit = {
    val index = createIndex {
      "-language:Scala2" +: whitelistedFiles
    }

    buildDocs(
      "./build/docs/",
      templatePath,
      resources,
      index
    )
  }
}
