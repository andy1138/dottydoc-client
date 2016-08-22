package dotty.tools.doc.client

import org.scalajs.dom.{ document, KeyboardEvent }
import org.scalajs.dom.html.{ Div, Input }
import org.scalajs.dom.ext.KeyCode

case object KeyBindings {
  private val mainContainer: Div =
    document.getElementById("main-container").asInstanceOf[Div]

  private val searchField: Input =
    document.getElementById("search").asInstanceOf[Input]

  private val searchResults: Div =
    document.getElementById("search-results").asInstanceOf[Div]

  def handleMain(): KeyboardEvent => Unit = { event: KeyboardEvent =>
    event.keyCode match {
      case KeyCode.Tab =>
        event.preventDefault()
        searchField.focus()

      case KeyCode.Escape =>
        event.preventDefault()
        searchField.blur()

      case _ => () // Do nothing
    }
  }

  def bind(): Unit = {
    searchField.onkeyup = new Search(searchField).search()
    mainContainer.onkeyup = handleMain()
  }
}
