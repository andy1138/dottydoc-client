package dotty.tools.doc.client
package html

import scala.scalajs.{ js => sjs }
import scalatags.JsDom.all._
import CustomTags._
import model.ops._
import model._

case class MainFragment(entity: Entity) extends Fragment {
  def render = mainContainer.render

  val packageName = entity.path.dropRight(1).mkString(".")

  val entityName =
    if (entity.kind == "package") entity.name.split("\\.").last
    else entity.name

  val companionAnchor = util.linking.companion(entity).map { c =>
    a(
      cls := "mdl-navigation__link",
      href := c.path.last + ".html",
      "Companion " + c.kind
    )
  }

  val mainContainer = div(
    cls := "mdl-layout mdl-js-layout mdl-layout--fixed-drawer",
    div(
      cls := "mdl-layout__drawer",
      span(cls := "mdl-layout-title subtitle", packageName),
      span(cls := "mdl-layout-title",entityName),
      nav(
        cls := "related mdl-navigation",
        companionAnchor,
        a(cls := "mdl-navigation__link", href := "#", "Source")
      ),
      span(cls := "mdl-layout-title", id := "docs-title", "Docs"),
      SearchFragment.render,
      PackageFragment(entity).render
    ),
    main(id := "entity-container", cls := "mdl-layout__content", EntityFragment(entity).render),
    main(id := "search-results", cls := "mdl-layout__content")
  )
}
