package dotty.tools.doc.client
package util

import scala.scalajs.{ js => sjs }
import scalatags.JsDom.all._
import org.scalajs.dom.html.Span
import model._
import model.ops._

object linking {
  import EntityIndex.packages

  def linkTo(path: sjs.Array[String], from: Entity): String = {
    val offset = from.kind match {
      case "val" | "def" => 2
      case _ => 1
    }

    val prefix = from.kind match {
      case "package" => "../"
      case _ => ""
    }

    prefix + {
      "../" * (from.path.length - offset) + path.mkString("","/",".html")
    }
  }

  def companion(entity: Entity) = {
    val pack = entity.path.dropRight(1).mkString(".")
    for {
      p     <- packages.get(pack)
      child <- p.children.find(e => e.name == entity.name && e.path.last != entity.path.last)
    } yield child
  }

  def referenceToLink(ref: Reference): Span = {
    def linkToAnchor(link: MaterializableLink) = link match {
      case MaterializedLink(target) => a(href := target, link.title)
      case NoLink(_) => span(link.title)
      case UnsetLink(query) =>
        println("UnsetLink found:")
        scala.scalajs.js.Dynamic.global.console.log(link)
        span(link.title)
    }

    ref match {
      case TypeReference(tref) =>
        val infixTypes = "<:<" :: "=:=" :: Nil
        if (tref.paramLinks.length == 2 && infixTypes.contains(tref.title)) span(
          referenceToLink(tref.paramLinks(0)),
          span(cls := "type-separator no-left-margin"),
          linkToAnchor(tref.tpeLink),
          span(cls := "type-separator no-left-margin"),
          referenceToLink(tref.paramLinks(1))
        ).render
        else if (tref.paramLinks.nonEmpty) span(
          linkToAnchor(tref.tpeLink),
          "[",
          tref
            .paramLinks
            .map(referenceToLink)
            .flatMap(link => Seq(link, span(cls := "type-separator no-left-margin", ",").render))
            .toList.dropRight(1),
          "]"
        ).render
      else span(linkToAnchor(tref.tpeLink)).render

      case OrTypeReference(left, right) =>
        span(
          referenceToLink(left),
          span(cls := "type-separator", "|"),
          referenceToLink(right)
        ).render

      case AndTypeReference(left, right) =>
        span(
          referenceToLink(left),
          span(cls := "type-separator", "&"),
          referenceToLink(right)
        ).render

      case BoundsReference(low, high) =>
        span(
          referenceToLink(low),
          span(cls := "type-separator", "<:"),
          referenceToLink(high)
        ).render

      case FunctionReference(func) => {
        span(
          cls := "no-left-margin",
          if (func.args.length > 1) "(" else "",
          if (func.args.isEmpty) span("()")
          else
            func.args.map(referenceToLink).flatMap{ link =>
              Seq(link, span(cls := "type-separator no-left-margin", ",").render)
            }.init.toList,
          if (func.args.length > 1) ") => " else " => ",
          referenceToLink(func.returnValue)
        ).render
      }

      case TupleReference(tref) => {
        span(
          cls := "no-left-margin",
          "(",
          tref.args.map(referenceToLink).flatMap{ link =>
            Seq(link, span(cls := "type-separator no-left-margin", ",").render)
          }.init.toList,
          ")"
        ).render
      }
    }
  }
}
