package dotty.tools.doc.client
package html

import scala.scalajs.{ js => sjs }
import scalatags.JsDom.all._
import model._
import model.ops._
import util.linking._

case class EntityFragment(entity: Entity) extends Fragment {
  def render = pageContent.render

  val members = entity match {
    case Members(mbrs) if mbrs.nonEmpty =>
      Seq(
        h5("Members"),
        div(
          cls := "mdl-grid",
          mbrs
            .collect { case x if x.hasModifiers && !x.isPrivate => x }
            .map(MemberFragment(_, entity).render).toList
          )
        )
    case _ => Seq()
  }

  val constructors = {
    def paramLists(xs: sjs.Array[ParamList]) =
      for {
        plist <- xs
        start =
          if (plist.isImplicit) "(implicit" else "("
        listCls =
          if (plist.isImplicit) "member-param-list is-implicit no-left-margin"
          else "member-param-list no-left-margin"
      } yield {
        span(cls := listCls, start) ::
        plist.list.map(e => span(
          cls := "no-left-margin",
          e.title,
          if (e.isByName) ": => "
          else ": ",
          referenceToLink(e.ref),
          if (e.isRepeated) "*"
          else ""
        )).toList ++
        Seq(span(cls := "no-left-margin", ")"))
      }

    def templ(xs: sjs.Array[sjs.Array[ParamList]]) = div(
      for {
        constructor <- xs.toList
      } yield div(
        cls := "member-definition",
        span(cls := "member-name", s"new ${entity.name}"),
        paramLists(constructor).toList
      )
    )

    entity match {
      case Class(c) if c.constructors.nonEmpty =>
        Seq(h5("Constructors"), templ(c.constructors))
      case CaseClass(c) if c.constructors.nonEmpty =>
        Seq(h5("Constructors"), templ(c.constructors))
      case _ =>
        Nil
    }
  }

  val pageContent = div(
    cls := "page-content",
    TitleFragment(entity).render,
    div(raw(entity.comment.fold("")(_.body))),
    entity.comment.filter(_.authors.nonEmpty).map { comment =>
      dl(
        dt(cls := "entity-authors", "Authors"),
        comment.authors.map(x => dd(cls := "entity-author", raw(x))).toList
      )
    }.toOption,
    constructors,
    members
  )
}
