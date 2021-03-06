package dotty.tools.doc.client
package model

import scala.scalajs.{ js => sjs }
import sjs.annotation.ScalaJSDefined

/** This file defines the interface for which to interact with the searchable
 *  index. To use the normal operations available on the traits on the JVM:
 *
 *  {{{
 *  import dotty.tools.dottydoc.js.model.ops._
 *  val x: Package = ...
 *  }}}
 *
 *  Please note that some of the actual fields have not been added to this
 *  interface, this is simply due to the fact that they're not necessary for
 *  search - YET. They could be added, for instance `comment` is missing.
 */
@ScalaJSDefined
trait Entity extends sjs.Object {
  val kind: String
  val name: String
  val path: sjs.Array[String]
  val comment: sjs.UndefOr[Comment]
}

/** This trait is used to be able to pattern match on façade like:
 *  {{{
 *  entity match {
 *    case Package(p) => "Found package!"
 *    case Class(c)   => "Found class!"
 *    ...
 *  }
 *  }}}
 */
trait EntityExtractor[E] {
  def rightKind: String => Boolean

  def extract: Entity => E = _.asInstanceOf[E]

  def unapply(e: Entity): Option[E] =
    if (rightKind(e.kind)) Some(extract(e))
    else None
}

@ScalaJSDefined
trait Comment extends sjs.Object {
  val body:                    String
  val short:                   String
  val authors:                 sjs.Array[String]
  val see:                     sjs.Array[String]
  val result:                  sjs.UndefOr[String]
  val throws:                  Map[String, String]
  val valueParams:             Map[String, String]
  val typeParams:              Map[String, String]
  val version:                 sjs.UndefOr[String]
  val since:                   sjs.UndefOr[String]
  val todo:                    List[String]
  val deprecated:              sjs.UndefOr[String]
  val note:                    List[String]
  val example:                 List[String]
  val constructor:             sjs.UndefOr[String]
  val group:                   sjs.UndefOr[String]
  val groupDesc:               Map[String, String]
  val groupNames:              Map[String, String]
  val groupPrio:               Map[String, String]
  val hideImplicitConversions: List[String]
}

@ScalaJSDefined
trait Members extends sjs.Object {
  val members: sjs.Array[Entity]
}

object Members extends EntityExtractor[sjs.Array[Entity]] {
  def rightKind = ops.EntitiesWithMembers.contains(_)
  override def extract = _.asInstanceOf[Members].members
}

@ScalaJSDefined
trait Modifiers extends sjs.Object {
  val modifiers: sjs.Array[String]
}

object Modifiers extends EntityExtractor[sjs.Array[String]] {
  def rightKind = ops.EntitiesWithModifiers.contains(_)
  override def extract = _.asInstanceOf[Modifiers].modifiers
}

@ScalaJSDefined
trait ReturnValue extends sjs.Object {
  val returnValue: Reference
}

@ScalaJSDefined
trait TypeParams extends sjs.Object {
  val typeParams: sjs.Array[String]
}

object TypeParams extends EntityExtractor[sjs.Array[String]] {
  def rightKind = ops.EntitiesWithTypeParams.contains(_)
  override def extract = _.asInstanceOf[TypeParams].typeParams
}

@ScalaJSDefined
trait Constructors extends sjs.Object {
  def constructors: sjs.Array[sjs.Array[ParamList]]
}

@ScalaJSDefined
trait SuperTypes extends sjs.Object {
  val superTypes: sjs.Array[MaterializableLink]
}

object SuperTypes extends EntityExtractor[sjs.Array[MaterializableLink]] {
  def rightKind = ops.EntitiesWithSuperTypes.contains(_)
  override def extract = _.asInstanceOf[SuperTypes].superTypes
}


@ScalaJSDefined
trait Package extends Entity with Members

object Package extends EntityExtractor[Package] {
  def rightKind = _ == "package"
}

@ScalaJSDefined
trait Class extends Entity with Members with Modifiers with TypeParams with Constructors

object Class extends EntityExtractor[Class] {
  def rightKind = _ == "class"
}

@ScalaJSDefined
trait CaseClass extends Class

object CaseClass extends EntityExtractor[CaseClass] {
  def rightKind = _ == "case class"
}

@ScalaJSDefined
trait Object extends Entity with Members with Modifiers

object Object extends EntityExtractor[Object] {
  def rightKind = _ == "object"
}

@ScalaJSDefined
trait Trait extends Class

object Trait extends EntityExtractor[Trait] {
  def rightKind = _ == "trait"
}

@ScalaJSDefined
trait ParamList extends sjs.Object {
  val list: sjs.Array[NamedReference]
  val isImplicit: Boolean
}

@ScalaJSDefined
trait Def extends Entity with Modifiers with ReturnValue {
  val typeParams: sjs.Array[String]
  val paramLists: sjs.Array[ParamList]
  val implicitlyAddedFrom: sjs.UndefOr[Reference]
}

object Def extends EntityExtractor[Def] {
  def rightKind = _ == "def"
}

@ScalaJSDefined
trait Val extends Entity with Modifiers {
  val implicitlyAddedFrom: sjs.UndefOr[Reference]
}

object Val extends EntityExtractor[Val] {
  def rightKind = _ == "val"
}

@ScalaJSDefined
trait Var extends Entity with Modifiers {
  val implicitlyAddedFrom: sjs.UndefOr[Reference]
}

object Var extends EntityExtractor[Var] {
  def rightKind = _ == "var"
}

@ScalaJSDefined
trait ImplicitlyAddedEntity extends Entity {
  val implicitlyAddedFrom: sjs.UndefOr[Reference]
}

object ops {
  val EntitiesWithModifiers =
    "case class" :: "class" :: "object" :: "trait" :: "def" :: "val" :: Nil

  val EntitiesWithMembers =
    "package" :: "case class" :: "class" :: "object" :: "trait" :: Nil

  val EntitiesWithTypeParams =
    "case class" :: "class" :: "trait" :: "def" :: Nil

  val EntitiesWithSuperTypes =
    "case class" :: "class" :: "trait" :: "object" :: Nil

  implicit class PackageOps(val p: Package) {
    def children: sjs.Array[Entity with Members] =
      p.members.collect {
        case x if EntitiesWithMembers contains x.kind =>
          x.asInstanceOf[Entity with Members]
      }

    def withMembers(mbrs: sjs.Array[Entity]): Package = new Package {
      val kind = p.kind
      val name = p.name
      val path = p.path
      val members = mbrs
      val comment = p.comment
    }
  }

  implicit class EntityOps(val ent: Entity) {
    def typeParams: sjs.Array[String] =
      if (ent.kind == "def")
        ent.asInstanceOf[Def].typeParams
      else sjs.Array()


    def modifiers: sjs.Array[String] =
      if (hasModifiers) ent.asInstanceOf[Modifiers].modifiers
      else sjs.Array()

    def hasMembers: Boolean =
      EntitiesWithMembers contains ent.kind

    def hasModifiers: Boolean =
      EntitiesWithModifiers contains ent.kind

    def hasTypeParams: Boolean =
      EntitiesWithTypeParams contains ent.kind

    def hasSuperTypes: Boolean =
      EntitiesWithSuperTypes contains ent.kind

    def isPrivate: Boolean =
      hasModifiers &&
      ent.asInstanceOf[Modifiers].modifiers.contains("private")

    def addedImplicitly: Boolean = (ent.kind == "def" || ent.kind == "val") && {
      ent.asInstanceOf[ImplicitlyAddedEntity].implicitlyAddedFrom.isDefined
    }

    def foldImplicitlyAdded[B](f: Reference => B): sjs.UndefOr[B] =
      if (ent.kind == "def" || ent.kind == "val") ent.asInstanceOf[ImplicitlyAddedEntity].implicitlyAddedFrom.map(f)
      else sjs.undefined
  }
}
