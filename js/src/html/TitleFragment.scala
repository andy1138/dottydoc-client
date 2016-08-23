package dotty.tools.doc.client
package html

import scalatags.JsDom.all._
import model._

case class TitleFragment(entity: Entity) extends Fragment {
  def render = title.render

  val modifiers = entity match {
    case Modifiers(mods) if mods.nonEmpty =>
      Some(span(cls := "entity-modifiers", mods.mkString(" ")))
    case _ =>
      None
  }

  val typeParams = entity match {
    case TypeParams(params) if params.nonEmpty =>
      Some(span(cls := "entity-type-params no-left-margin", params.mkString("[", ", ", "]")))
    case _ => None
  }

  val superTypes = entity match {
    case SuperTypes(supers) if supers.nonEmpty =>
      Some(div(
        cls := "entity-super-types",
        span(cls := "keyword", "extends "),
        span(supers.head.title),
        supers.tail.flatMap { superTpe =>
          span(cls := "keyword", "with ") :: span(superTpe.title) :: Nil
        }.toList
      ))
    case _ => None
  }

  val title = div(
    cls := "entity-title",
    modifiers,
    span(cls := "entity-kind", entity.kind),
    span(cls := "entity-name", entity.name),
    typeParams,
    superTypes
  )
}
