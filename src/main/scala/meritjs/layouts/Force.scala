package meritjs.layouts

import meritjs.{Graph, JSMeritLink, JSMeritNode, User}
import org.singlespaced.d3js.forceModule.Node
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.{Link, Selection, d3}

import scala.scalajs.js.Array
import scalajs.js.JSConverters._

/**
  * Created by gante on 22.01.17.
  */

class ForceNode(val id: String, val name: String, val sent: Option[Int], val received: Option[Int], override val fill: String = "#000") extends Node with User

class ForceLink(override val source: ForceNode, override val target: ForceNode, val amount: Double) extends Link[ForceNode]

object Force {
  def draw(users: Array[JSMeritNode], transactions: Array[JSMeritLink],
            width: Double, height: Double, split: Boolean = false)(implicit svg: Selection[EventTarget]): Graph = {
    val nodes = if(split) {
      users.flatMap(n => Array(
        new ForceNode(s"from: ${n.userId}", n.name, Option(n.sent), None, "#0f0"),
        new ForceNode(s"to: ${n.userId}", n.name, None, Option(n.received), "#f00")))
    }
    else {
      users.map(n => new ForceNode(n.userId, n.name, Option(n.sent), Option(n.received)))
    }

    def getLinks(senderPrefix: String = "", receiverPrefix: String = ""): Array[ForceLink] = {
      val nodeMap = (for (n <- nodes) yield (n.id, n)).toMap
      transactions.filter(l => nodeMap.contains(s"$senderPrefix${l.from}") && nodeMap.contains(s"$senderPrefix${l.to}"))
          .groupBy(l => (l.from,l.to))
          .map(t => new ForceLink(
            nodeMap(s"$senderPrefix${t._1._1}"),
            nodeMap(s"$receiverPrefix${t._1._2}"),
            t._2.map(_.amount).sum
          )).toJSArray
    }

    def setAttributes(link: Selection[ForceLink], node: Selection[ForceNode]) = {
      node.attr("transform", (n: ForceNode) => s"translate(${n.x},${n.y})")
        .attr("fill", (n: ForceNode) => n.fill)

      link.attr("stroke-width", (l: ForceLink) => l.amount)
        .attr("x1", (l: ForceLink) => l.source.x)
        .attr("y1", (l: ForceLink) => l.source.y)
        .attr("x2", (l: ForceLink) => l.target.x)
        .attr("y2", (l: ForceLink) => l.target.y)
    }

    val links = if(split) getLinks("from: ", "to: ") else getLinks()

    val force = d3.layout.force()
      .size(width, height)
      .gravity(0.05)
      .linkDistance(width/3.05)
      .charge(-100)
      .nodes(nodes)
      .links(links)

    val link = addData(links, "link", "line")
    val node = addData(nodes, "node", "g").call(force.drag)

    node.append("circle")
      .attr("class", "node")
      .attr("r", (n: ForceNode) => 3*math.sqrt(math.max(1, n.received.getOrElse(n.sent.getOrElse((0))))))

    node.append("text")
      .attr("dx", (n: ForceNode) => 9 + 3*math.sqrt(math.max(1, n.received.getOrElse(n.sent.getOrElse(0)))))
      .attr("dy", "0.35em")
      .text((n: ForceNode) => n.getLabel)

    node.append("title").text((n: ForceNode) => n.getTooltip)

    force.on("tick", _ => setAttributes(link, node))
    force.start()

    Graph(users)
  }
}
