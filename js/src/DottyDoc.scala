package dotty.tools.doc
package client

import scala.scalajs.{ js => sjs }
import sjs.annotation.{ JSExport, JSName }
import org.scalajs.dom.html.{ Div, Input }
import org.scalajs.dom.document
import html.MainFragment

@JSExport object DottyDoc {
  @JSExport def main(target: Div) = {
    document.title = "Dotty " + EntityIndex.currentEntity.path.mkString(".")
    target.appendChild(MainFragment(EntityIndex.currentEntity).render)
    hljs.initHighlightingOnLoad()

    val searchInput = document.getElementById("search").asInstanceOf[Input]
    searchInput.onkeyup = new Search(searchInput).search()
  }
}

/** Library wrapper for highlighting */
@sjs.native object hljs extends sjs.Object {
  def initHighlightingOnLoad(): sjs.Any = sjs.native
}
