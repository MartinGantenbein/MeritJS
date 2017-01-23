package meritjs

import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.{Selection, d3}
import org.singlespaced.d3js.Ops._


import scala.scalajs.js.Array

/**
  * Created by gante on 22.01.17.
  */
trait ForceLayout {
  def createForceLayout(nodes: Array[MeritNode], links: Array[MeritLink]) = {
    val svg = d3.select("#force")
    val width: Double = java.lang.Double.valueOf(svg.attr("width"))
    val height: Double = java.lang.Double.valueOf(svg.attr("height"))

    println(nodes.length)
    println(links.length)

    val force = d3.layout.force()
      .size((width, height))
      .nodes(nodes)
      .links(links)

    force.linkDistance(width / 2.0)

    val link = addData(svg, links, "links", "line")
    val node = addData(svg, nodes, "nodes", "circle")

    node.append("title").text((n: MeritNode) => s"${n.id} (${n.balance})")

    force.on("end", _ => setAttributes(link, node))
    force
  }

  private def addData[T](svg: Selection[EventTarget], data: Array[T], cssClass: String, svgObject: String) = {
    val sel = svg.selectAll(s".$cssClass")
      .data(data)
      .enter().append(svgObject)
      .attr("class", cssClass)
    sel
  }

  private def setAttributes(link: Selection[MeritLink], node: Selection[MeritNode]) = {
    node.attr("r", 5)
      .attr("cx", (n: MeritNode) => n.x)
      .attr("cy", (n: MeritNode) => n.y)

    link.attr("stroke-width", (l: MeritLink) => Math.sqrt(l.amount))
      .attr("x1", (l: MeritLink) => l.source.x)
      .attr("y1", (l: MeritLink) => l.source.y)
      .attr("x2", (l: MeritLink) => l.target.x)
      .attr("y2", (l: MeritLink) => l.target.y)
  }

}
