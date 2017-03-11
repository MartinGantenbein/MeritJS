package meritjs.layouts

import meritjs.{Graph, JSMeritNode, User}
import org.scalajs.dom.EventTarget
import org.scalajs.dom.raw.{SVGPathElement, SVGTextContentElement}
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.chordModule.{Group, Node}
import org.singlespaced.d3js.svg.Arc
import org.singlespaced.d3js.{Selection, d3}
import org.singlespaced.d3js.svg.chordModule.{Link => svgLink, Node => svgNode}

import scala.scalajs.js
import scala.scalajs.js.{Array, UndefOr}
import js.Dynamic.global

/**
  * Created by mg on 26.01.2017.
  */
object Chord {
  private[this] class ChordGroup(val id: String, val name: String, val sent: UndefOr[Int], val received: UndefOr[Int], override val fill: String = "#000") extends User

  def draw(users: Array[JSMeritNode], matrix: Array[Array[(Double, String)]])(implicit svg: Selection[EventTarget]): Graph = {
    val fill = d3.scale.category20()
    val coloredUsers = users.zipWithIndex.map(u => new ChordGroup(u._1.userId, u._1.name, u._1.sent, u._1.received, fill(u._2.toString)))
/*
    val nodes = if(split) {
      users.flatMap(n => Array(
        new ForceNode(s"from: ${n.userId}", n.name, n.sent, null, "#0f0"),
        new ForceNode(s"to: ${n.userId}", n.name, null, n.received, "#f00")))
    }
    else {
      users.map(n => new ForceNode(n.userId, n.name, n.sent, n.received))
    }
*/

    val width = svg.attr("width").toDouble
    val height = svg.attr("height").toDouble

    val outerRadius = math.min(width, height) / 2 - 10
    val innerRadius = outerRadius - 24
    val arc: Arc[JSMeritNode] = d3.svg.arc()
      .innerRadius(innerRadius)
      .outerRadius(outerRadius)
    val layout = d3.layout.chord()
      .padding(.04)
      .sortSubgroups((d1, d2) => d1.compareTo(d2))
      .sortChords((d1, d2) => d2.compareTo(d1))
    val path = d3.svg.chord()
      .radius(innerRadius)

    val g = svg.append("g")
      .attr("id", "circle")
      .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")

    g.append("circle")
      .attr("r", outerRadius)

    layout.matrix(matrix.map(_.map(_._1)))

    // Add a group per neighborhood.
    val group: Selection[Group] = addData(layout.groups(), "group", "g")(g)

    group.append("title")
      .text((_: js.Object, i: Int) => coloredUsers(i).getTooltip())

    // Add the group arc
    val groupPath = group.append("path")
      .attr("id", (_: js.Object, i: Int) => coloredUsers(i).id)
      .attr("d", (n: js.Object, i: Int) => arc(n.asInstanceOf[JSMeritNode], i))
      .style("fill", (_: js.Object, i: Int) => coloredUsers(i).fill)

    // Add a text label
    val groupText: Selection[Group] = group
      .append("text")
      .attr("x", 6)
      .attr("dy", 15)
      .attr("dx", 10)

    groupText
      .append("textPath")
      .attr("xlink:href", (d: js.Object, i: Int) => s"#${coloredUsers(i).id}" )
      .text((d: js.Object, i: Int) => "")

    def chooseLabel(element: SVGTextContentElement, availableLength: Double, labels: List[String]): Unit = {
      labels match {
          case Nil => element.childNodes(0).textContent = ""
          case _ =>
            element.childNodes(0).textContent = labels.head
            if (availableLength < element.getComputedTextLength())
              chooseLabel(element, availableLength, labels.tail)
      }
    }

    for(i <- 0 until groupPath(0).length) {
      val t = groupText(0)(i).asInstanceOf[SVGTextContentElement]
      val availableLength = groupPath(0)(i).asInstanceOf[SVGPathElement].getTotalLength() / 2 - 36

      chooseLabel(t, availableLength, coloredUsers(i).getLabelAlternatives)
    }

    // Add the chords
    val chord = addData(layout.chords(), "chord", "path")(g)
      // Properly typed implementation does not work with d3js 0.3.4
      .style("fill", (d: Any, i: Int) => coloredUsers(d.asInstanceOf[svgLink[Node]].source.index.toInt).fill)
      .attr("d", (d: Any, i: Int) => path.apply(d.asInstanceOf[svgLink[svgNode]], i))

    // Add an elaborate mouseover title for each chord.
    chord.append("title").text((d: Any, _: Int) => {
      val l = d.asInstanceOf[svgLink[Node]]
      s"${matrix(l.source.index.toInt)(l.target.index.toInt)._2}\n${matrix(l.target.index.toInt)(l.source.index.toInt)._2}"
    })

    // fading others on mouseover
    // this compiles but leads to JS error
/*
    group.on("mouseover", (_, i, _) => chord.classed("fade",
      (l, _, _) => l.source.index != i && l.target.index != i)
    )
*/
    g.on("mouseleave", (_, _, _) => chord.classed("fade", value = false))

    Graph(group, chord)
  }
}
