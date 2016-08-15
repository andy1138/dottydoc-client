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

  val scala2Lib: Array[String] =
    getResource("/scala-collections.whitelist")
    .lines
    .foldLeft(Array.empty[String]) { (acc, line) =>
      if (line.startsWith("#")) acc
      else if (line.isEmpty) acc
      else acc :+ line
    }

  val resources: List[String] =
    getResource("/template.html")
    .parent
    .list
    .map(_.path.toAbsolutePath.toString)
    .toList

  override def main(args: Array[String]): Unit = {
    println(resources)
    val index = createIndex {
      "-language:Scala2" +: scala2Lib.toArray
    }

    buildDocs(
      "./build/docs/",
      templatePath,
      resources,
      index
    )
  }
}
