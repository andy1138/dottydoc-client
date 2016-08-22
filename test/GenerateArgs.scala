package dotty.tools.doc
package test

import dotty.tools.dottydoc.api.scala.Dottydoc

trait GenerateArgs extends GenerateCollections {
  override def main(args: Array[String]): Unit = {
    val index = createIndex {
      "-language:Scala2" +: args
    }

    buildDocs(
      "./build/docs/",
      templatePath,
      resources,
      index
    )
  }
}

object ArgsMain extends GenerateArgs
