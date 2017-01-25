package meritjs.layouts

import meritjs.{JSMeritLink, JSMeritNode}
import org.singlespaced.d3js.forceModule.Node
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.{Link, Selection, d3}

import scala.scalajs.js.Array

/**
  * Created by gante on 22.01.17.
  */

class ForceNode(val id: String, val name: String, val sent: Option[Int], val received: Option[Int], val fill: String = "#000") extends Node {
  def getLabel: String = {
    (sent, received) match {
      case (None, None) => s"$id"
      case (Some(s), None) => s"$id (-$s)"
      case (None, Some(r)) => s"$id (+$r)"
      case (Some(s), Some(r)) => s"$id (+$r / -$s)"
    }
  }
}

class ForceLink(val source: ForceNode, val target: ForceNode, val amount: Double) extends Link[ForceNode]

class Force(val svg: Selection[EventTarget], val users: Array[JSMeritNode], val transactions: Array[JSMeritLink],
            width: Double, height: Double, split: Boolean = false)
{
  private[this] val nodes = if(split) {
    users.flatMap(n => Array(
      new ForceNode(s"from: ${n.userId}", n.name, Option(n.sent), None, "#0f0"),
      new ForceNode(s"to: ${n.userId}", n.name, None, Option(n.received), "#f00")))
  }
  else {
    users.map(n => new ForceNode(n.userId, n.name, Option(n.sent), Option(n.received)))
  }

  def getLinks(senderPrefix: String = "", receiverPrefix: String = "") = {
    val nodeMap = (for (n <- nodes) yield (n.id, n)).toMap
    transactions.filter((l) => !l.booked && nodeMap.contains(s"$senderPrefix${l.from}") && nodeMap.contains(s"$senderPrefix${l.to}"))
      .map(l => new ForceLink(
        nodeMap(s"$senderPrefix${l.from}"),
        nodeMap(s"$receiverPrefix${l.to}"),
        l.amount))
  }

  private[this] val links = if(split) getLinks("from: ", "to: ") else getLinks()


  private[this] val force = d3.layout.force()
    .size(width, height)
    .gravity(0.05)
    .linkDistance(width/3.05)
    .charge(-100)
    .nodes(nodes)
    .links(links)

  private[this] val link = addData(links, "link", "line")
  private[this] val node = addData(nodes, "node", "g").call(force.drag)

  node.append("circle")
    .attr("class", "node")
    .attr("r", (n: ForceNode) => 3*math.sqrt(math.max(1, n.received.getOrElse(n.sent.getOrElse((0))))))

  node.append("text")
    .attr("dx", (n: ForceNode) => 9 + 3*math.sqrt(math.max(1, n.received.getOrElse(n.sent.getOrElse(0)))))
    .attr("dy", "0.35em")
    .text((n: ForceNode) => n.getLabel)

  node.append("title").text((n: ForceNode) => s"${n.name} (sent: ${n.sent}, received: ${n.received})")

  force.on("tick", _ => setAttributes(link, node))
  force.start()

  // TODO: extract into superclass
  private[this] def addData[T](data: Array[T], cssClass: String, svgObject: String) = {
    val sel = svg.selectAll(s".$cssClass")
      .data(data)
      .enter().append(svgObject)
      .attr("class", cssClass)
    sel
  }

  private[this] def setAttributes(link: Selection[ForceLink], node: Selection[ForceNode]) = {
    node.attr("transform", (n: ForceNode) => s"translate(${n.x},${n.y})")
      .attr("fill", (n: ForceNode) => n.fill)

    link.attr("stroke-width", (l: ForceLink) => l.amount)
      .attr("x1", (l: ForceLink) => l.source.x)
      .attr("y1", (l: ForceLink) => l.source.y)
      .attr("x2", (l: ForceLink) => l.target.x)
      .attr("y2", (l: ForceLink) => l.target.y)
  }
}
