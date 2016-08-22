package dotty.tools.doc.client
package html

import scala.scalajs.{ js => sjs }
import scalatags.JsDom.all._
import org.scalajs.dom.raw.HTMLElement
import model.Entity

trait Fragment {
  def render: HTMLElement
}
