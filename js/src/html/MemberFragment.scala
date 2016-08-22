package dotty.tools.doc.client
package html

import model._
import model.ops._
import util.linking._
import scalatags.JsDom.all._
import scalatags.JsDom.TypedTag
import org.scalajs.dom
import org.scalajs.dom.html.{Div, Span}

case class MemberFragment(entity: Entity, parent: Entity) extends Fragment {
  def render = member.render

  val shortComment = div(
    cls := "mdl-cell mdl-cell--12-col summary-comment",
    raw(entity.comment.fold("")(_.short))
  ).render

  val fullComment = div(
    cls := "mdl-cell mdl-cell--12-col full-comment",
    style := "display: none;",
    entity.foldImplicitlyAdded { ref =>
      span("Implicitly added from: ", referenceToLink(ref))
    }.toOption,
    raw(entity.comment.fold("")(_.body))
  ).render

  val hasLongerFullComment = entity.comment.fold(false) { c =>
    c.short.length + 5 < c.body.length
  }

  val modifiers =
    if (entity.hasModifiers) Some(span(
      cls := "member-modifiers-kind",
      entity.modifiers.mkString(" ") + " " + entity.kind
    ))
    else None

  val typeParams = entity match {
    case Def(d) if d.typeParams.nonEmpty =>
      Some(span(cls := "member-type-params no-left-margin", d.typeParams.mkString("[", ", ", "]")))
    case _ => None
  }

  val paramList = entity match {
    case Def(d) if d.paramLists.nonEmpty =>
      Some(
        span(cls := "member-param-list no-left-margin",
          span(
            cls := "member-param-lists",
            d.paramLists.map { xs =>
              span(
                cls := "param-list",
                "(",
                span(cls := "is-implicit no-left-margin", if (xs.isImplicit) "implicit " else ""),
                xs.list.flatMap { tr =>
                  Seq(
                    span(cls := "param-name", tr.title).render,
                    span(cls := "type-separator no-left-margin", if (tr.isByName) ": =>" else ":").render,
                    span(if (tr.ref.kind == "FunctionReference" && tr.isRepeated) "(" else "").render,
                    span(referenceToLink(tr.ref)).render,
                    span(if (tr.ref.kind == "FunctionReference" && tr.isRepeated) ")*" else if (tr.isRepeated) "*" else "").render,
                    span(cls := "type-separator no-left-margin", ",").render
                  )
                }.toList.dropRight(1),
                ")"
              ).render
            }.toList
          )
        )
      )
    case _ => None
  }

  val returnValue = {
    // shortens: "Option.this.A" => "A"
    def shorten(s: String): String = s.split('.').toList match {
      case x :: Nil => x
      case x :: xs if x == parent.name => xs.last
      case xs => s
    }

    def link(rv: Reference): Span = {
      def decodeTpeLink(link: MaterializableLink): Span = link.kind match {
        case "MaterializedLink" =>
          val ml = link.asInstanceOf[MaterializedLink]
          span(cls := "member-return-value", a(href := ml.target, ml.title)).render
        case "UnsetLink" =>
          val un = link.asInstanceOf[UnsetLink]
          span(cls := "member-return-value", shorten(un.query)).render
        case "NoLink" =>
          val no = link.asInstanceOf[NoLink]
          span(cls := "member-return-value", shorten(no.title)).render
      }

      rv match {
        case TypeReference(trv) =>
          val returnValue = decodeTpeLink(trv.tpeLink)

          if (trv.paramLinks.nonEmpty) span(
            returnValue,
            "[",
            trv.paramLinks
              .map(link)
              .flatMap { sp =>
                Seq(sp, span(cls := "type-separator no-left-margin", ",").render)
              }
              .toList.dropRight(1),
            "]"
          ).render
          else returnValue

        case OrTypeReference(left, right) =>
          span(
            cls := "member-return-value or-type",
            link(left),
            span(cls := "type-separator", "|"),
            link(right)
          ).render
        case AndTypeReference(left, right) =>
          span(
            cls := "member-return-value and-type",
            link(left),
            span(cls := "type-separator", "&"),
            link(right)
          ).render

        case BoundsReference(low, high) =>
          span(
            link(low),
            span(cls := "type-separator", "<:"),
            link(high)
          ).render
        case FunctionReference(func) =>
          span(
            cls := "no-left-margin",
            if (func.args.length > 1) "(" else "",
            if (func.args.isEmpty)
              span("()")
            else func
              .args
              .map(link)
              .flatMap(link => Seq(link, span(cls := "type-separator no-left-margin", ",").render)).init.toList,
            if (func.args.length > 1) ") => " else " => ",
            link(func.returnValue)
          ).render

        case TupleReference(tuple) => {
          span(
            cls := "no-left-margin",
            "(",
            tuple
              .args
              .map(link)
              .flatMap(link => Seq(link, span(cls := "type-separator no-left-margin", ",").render)).init.toList,
            ")"
          ).render
        }
      }
    }

    entity match {
      case Def(d) =>
        Some(span(cls := "no-left-margin", ": ", link(d.returnValue)))
      case _ => None
    }
  }

  val member = {
    def toggleBetween(short: Div, and: Div): Unit =
      if (and.style.display == "none") {
        and.style.display = "block"
        short.style.display = "none"
      } else {
        and.style.display = "none"
        short.style.display = "block"
      }

    div(
      cls :=
        s"""
        mdl-cell mdl-cell--12-col member
        ${if (hasLongerFullComment) "member-fullcomment" else ""}
        """,
      onclick := { () => toggleBetween(shortComment, and = fullComment) },
      div(
        cls := "mdl-cell mdl-cell--12-col member-definition",
        modifiers,
        span(
          cls := { if (entity.addedImplicitly) "member-name implicitly-added" else "member-name" },
          entity.name
        ),
        typeParams,
        paramList,
        returnValue
      ),
      shortComment,
      fullComment
    )
  }

}
