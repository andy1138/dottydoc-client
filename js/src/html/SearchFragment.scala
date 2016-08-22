package dotty.tools.doc.client
package html

import scalatags.JsDom.all._

case object SearchFragment extends Fragment {
  def render = view.render

  val view =
    div(
      cls := "search-container",
      div(
        cls := "mdl-textfield mdl-js-textfield mdl-textfield--floating-label",
        input(cls := "mdl-textfield__input", `type` := "text", id := "search"),
        label(cls := "mdl-textfield__label", `for` := "search", "Search")
      )
    )
}
