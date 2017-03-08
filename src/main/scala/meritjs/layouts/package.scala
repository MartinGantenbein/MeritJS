package meritjs

import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Selection

import scala.scalajs.js.Array

/**
  * Created by mg on 28.01.2017.
  */
package object layouts {
  def addData[T](data: Array[T], cssClass: String, svgObject: String)(implicit svg: Selection[EventTarget]): Selection[T] = {
    val sel = svg.selectAll(s".$cssClass")
      .data(data)
      .enter().append(svgObject)
      .attr("class", cssClass)
    sel
  }
}

trait User {
  val id: String
  val name: String
  val sent: Option[Int]
  val received: Option[Int]
  val fill: String = "#000"

  def getLabel: String = {
    (sent, received) match {
      case (None, None) => s"$id"
      case (Some(s), None) => s"$id (-$s)"
      case (None, Some(r)) => s"$id (+$r)"
      case (Some(s), Some(r)) => s"$id (+$r / -$s)"
    }
  }

  def getTooltip: String = {
    (sent, received) match {
      case (None, None) => s"$name"
      case (Some(s), None) => s"$name (sent $s"
      case (None, Some(r)) => s"$name (received $r)"
      case (Some(s), Some(r)) => s"$name (received $r / sent $s)"
    }
  }
}

