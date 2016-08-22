package dotty.tools.doc.client
package model

import scala.scalajs.{ js => sjs }
import sjs.annotation.ScalaJSDefined

/** Type References */
@ScalaJSDefined
sealed trait Reference extends sjs.Object {
  val kind: String
}

/** This trait is used to be able to pattern match on façade like:
 *  {{{
 *  entity match {
 *    case TypeReference(ref)   => "Typeref!"
 *    case OrTypeReference(ref) => "Found OrTypeRef!"
 *    ...
 *  }
 *  }}}
 */
trait ReferenceExtractor[R] {
  def rightKind: String => Boolean

  def extract: Reference => R = _.asInstanceOf[R]

  def unapply(r: Reference): Option[R] =
    if (rightKind(r.kind)) Some(extract(r))
    else None
}

@ScalaJSDefined
trait TypeReference extends Reference {
  val title: String
  val tpeLink: MaterializableLink
  val paramLinks: sjs.Array[Reference]
}

object TypeReference extends ReferenceExtractor[TypeReference] {
  def rightKind = _ == "TypeReference"
}

@ScalaJSDefined
trait OrTypeReference extends Reference {
  val left: Reference
  val right: Reference
}

object OrTypeReference extends ReferenceExtractor[(Reference, Reference)] {
  override def extract = { r =>
    val ref = r.asInstanceOf[OrTypeReference]
    (ref.left, ref.right)
  }
  def rightKind = _ == "OrTypeReference"
}

@ScalaJSDefined
trait AndTypeReference extends Reference {
  val left: Reference
  val right: Reference
}

object AndTypeReference extends ReferenceExtractor[(Reference, Reference)] {
  override def extract = { r =>
    val ref = r.asInstanceOf[AndTypeReference]
    (ref.left, ref.right)
  }
  def rightKind = _ == "AndTypeReference"
}

@ScalaJSDefined
trait BoundsReference extends Reference {
  val low: Reference
  val high: Reference
}

object BoundsReference extends ReferenceExtractor[(Reference, Reference)] {
  override def extract = { r =>
    val ref = r.asInstanceOf[BoundsReference]
    (ref.low, ref.high)
  }
  def rightKind = _ == "AndTypeReference"
}

@ScalaJSDefined
trait NamedReference extends Reference {
  val title: String
  val ref: Reference
  val isByName: Boolean
  val isRepeated: Boolean
}

@ScalaJSDefined
trait ConstantReference extends Reference {
  val title: String
}

@ScalaJSDefined
trait FunctionReference extends Reference {
  val args: sjs.Array[Reference]
  val returnValue: Reference
}

object FunctionReference extends ReferenceExtractor[FunctionReference] {
  def rightKind = _ == "FunctionReference"
}

@ScalaJSDefined
trait TupleReference extends Reference {
  val args: sjs.Array[Reference]
}

object TupleReference extends ReferenceExtractor[TupleReference] {
  def rightKind = _ == "TupleReference"
}

/** Materializable links */
@ScalaJSDefined
sealed trait MaterializableLink extends sjs.Object {
  val kind: String
  val title: String
}

trait LinkExtractor[L] {
  def rightKind: String => Boolean

  def extract: MaterializableLink => L = _.asInstanceOf[L]

  def unapply(link: MaterializableLink): Option[L] =
    if (rightKind(link.kind)) Some(extract(link))
    else None
}

@ScalaJSDefined
trait UnsetLink extends MaterializableLink {
  val query: String
}

object UnsetLink extends LinkExtractor[String] {
  def rightKind = _ == "UnsetLink"
  override def extract = _.asInstanceOf[UnsetLink].query
}

@ScalaJSDefined
trait MaterializedLink extends MaterializableLink {
  val target: String
}

object MaterializedLink extends LinkExtractor[String] {
  def rightKind = _ == "MaterializedLink"
  override def extract = _.asInstanceOf[MaterializedLink].target
}

@ScalaJSDefined
trait NoLink extends MaterializableLink {
  val target: String
}

object NoLink extends LinkExtractor[String] {
  def rightKind = _ == "NoLink"
  override def extract = _.asInstanceOf[NoLink].target
}
