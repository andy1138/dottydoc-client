package dotty.tools.doc.client
package html

import scalatags.JsDom.all._
import model._
import model.ops._

case class PackageFragment(entity: Entity) extends Fragment {
  import EntityIndex.packages
  import util.linking._

  def render = view.render

  private[this] val productReg = """^Product[0-9]+$""".r

  def packUl(pack: Package) = ul(
    cls := "mdl-list packages",
    (for {
      ent <- pack.children
      if !(productReg.findFirstIn(ent.name).map(_.slice(7, 99).toInt > 3).getOrElse(false) ||
        // Filter out packages
        ent.kind == "package" ||
        // Filter out objects that have companions
        (ent.kind == "object" && companion(ent).isDefined) || ent.name == "AnyValCompanion")
    } yield {
      val comp = companion(ent)
      val entUrl = linkTo(ent.path, from = entity)
      val compUrl = comp.map(_.path).map(linkTo(_, entity)).getOrElse("#")
      li(
        cls := s"""mdl-list__item entity ${ if (comp.isDefined) "two" else "one" }""",
        comp.map { _ => a(cls := "entity-button object", href := compUrl, "O") }.getOrElse(()),
        a(
          cls := s"""entity-button shadowed ${ent.kind.replaceAll(" ", "")}""",
          href := entUrl,
          ent.kind(0).toUpper.toString
        ),
        a(cls := "entity-name", href := entUrl, ent.name)
      )
    }).toList
  )

  val view = div(
    (for ((packName, pack) <- packages if pack.children.nonEmpty) yield div(
      id := s"""${packName.replaceAll("\\.", "-")}""",
      cls := "package-container",
      span(cls := "mdl-layout-title", a(href := linkTo(pack.path, entity, "/index.html"), packName)),
      packUl(pack)
    )).toList
  )
}
