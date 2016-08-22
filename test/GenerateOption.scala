package dotty.tools.doc
package test

import dotty.tools.dottydoc.api.scala.Dottydoc

trait GenerateArray extends GenerateCollections {
  override def whitelistedFiles =
    super.whitelistedFiles.filter(_.endsWith("scala/Array.scala"))
}

object ArrayMain extends GenerateArray
