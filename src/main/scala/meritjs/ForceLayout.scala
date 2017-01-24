package meritjs

import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.{Selection, d3}
import org.singlespaced.d3js.Ops._


import scala.scalajs.js.Array

/**
  * Created by gante on 22.01.17.
  */
trait ForceLayout {
  def createForceLayout(svgId: String, nodes: Array[MeritNode], links: Array[MeritLink])(implicit config: Config) = {
    val svg = d3.select(s"#$svgId")
    svg.attr("width", config.graph_width)
    svg.attr("height", config.graph_height)

    val force = d3.layout.force()
      .size((0.9*config.graph_width, 0.9*config.graph_height))
      .gravity(0.05)
      .linkDistance(config.graph_width / 3.05)
      .charge(-100)
      .nodes(nodes)
      .links(links)
    val link = addData(svg, links, "link", "line")
    val node = addData(svg, nodes, "node", "g").call(force.drag)

    node.append("circle")
      .attr("class", "node")
      .attr("r", 5)

    node.append("text")
      .attr("dx", 12)
      .attr("dy", "0.35em")
      .text((n: MeritNode) => s"${n.id} (+${n.received} / -${n.sent})")

    node.append("title").text((n: MeritNode) => s"${n.name} (sent: ${n.sent}, received: ${n.received})")

    force.on("tick", _ => setAttributes(link, node))
    force.start()
  }

  private def addData[T](svg: Selection[EventTarget], data: Array[T], cssClass: String, svgObject: String) = {
    val sel = svg.selectAll(s".$cssClass")
      .data(data)
      .enter().append(svgObject)
      .attr("class", cssClass)
    sel
  }

  private def setAttributes(link: Selection[MeritLink], node: Selection[MeritNode])(implicit cfg: Config) = {
    node.attr("transform", (n: MeritNode) => {
      s"translate(${n.x},${n.y})"
    })

    link.attr("stroke-width", (l: MeritLink) => l.amount)
      .attr("x1", (l: MeritLink) => l.source.x)
      .attr("y1", (l: MeritLink) => l.source.y)
      .attr("x2", (l: MeritLink) => l.target.x)
      .attr("y2", (l: MeritLink) => l.target.y)
  }
}
