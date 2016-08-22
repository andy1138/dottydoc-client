package dotty.tools.doc.client
package html

import scalatags.JsDom.all._
import model.Entity

case class PackageFragment(entity: Entity) extends Fragment {
  import EntityIndex.packages
  import model.ops._
  import util.linking._

  def render = view.render

  val view = ul(
    cls := "mdl-list packages",
    {
      val keys: Seq[String] = packages.keys.toSeq.sorted
      val productReg = """^Product[0-9]+$""".r
      keys.flatMap { k =>
        val pack = packages(k)
        val children =
          pack.children
            .sortBy(_.name)
            .filterNot { ent =>
              // Filter out ProductX where X > 3
              productReg.findFirstIn(ent.name).map(_.slice(7, 99).toInt > 3).getOrElse(false) ||
              // Filter out packages
              ent.kind == "package" ||
              // Filter out objects that have companions
              (ent.kind == "object" && companion(ent).isDefined) ||
              ent.name == "AnyValCompanion"
            }
            .map { ent =>
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
                a(
                  cls := "entity-name",
                  href := entUrl,
                  ent.name
                )
              )
            }

        if (children.nonEmpty)
          li(cls := "mdl-list__item package", href := linkTo(pack.path, entity), k) :: children.toList
        else Nil
      }
    }
  )
}
