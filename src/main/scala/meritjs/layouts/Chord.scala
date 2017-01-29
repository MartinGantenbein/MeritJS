package meritjs.layouts

import meritjs.{Graph, JSMeritLink, JSMeritNode, User}
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.chordModule.{Group, Node}
import org.singlespaced.d3js.{Selection, d3}
import org.singlespaced.d3js.svg.chordModule.Link

import scala.scalajs.js
import scala.scalajs.js.Array
import js.Dynamic.global

/**
  * Created by mg on 26.01.2017.
  */
object Chord {
  private[this] class ChordGroup(val id: String, val name: String, val sent: Option[Int], val received: Option[Int], override val fill: String = "#000") extends User

  def draw(users: Array[JSMeritNode], transactions: Array[JSMeritLink],
           width: Double, height: Double)(implicit svg: Selection[EventTarget]): Graph = {
    def getMerits(sender: JSMeritNode, receiver: JSMeritNode): Double = {
      transactions.filter(p => p.from == sender.userId && p.to == receiver.userId).map(_.amount).sum
    }

    val outerRadius = math.min(width, height) / 2 - 10
    val innerRadius = outerRadius - 24
    val arc = d3.svg.arc()
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

    val fill = d3.scale.category20()

    g.append("circle")
      .attr("r", outerRadius)

    layout.matrix(users.map(u => users.map(getMerits(_, u))))

    val coloredUsers: Array[ChordGroup] = users.zipWithIndex.map(u => new ChordGroup(u._1.userId, u._1.name, Option(u._1.sent), Option(u._1.received), fill(u._2.toString)))

    // Add a group per neighborhood.
    val group = addData(layout.groups(), "group", "g")(g)

    group.append("title")
      .text((_: js.Object, i: Int) => coloredUsers(i).getTooltip)

    // Add the group arc
    val groupPath = group.append("path")
      .attr("id", (_: js.Object, i: Int) => users(i).userId)
//      .attr("d", arc) // mg: does not compile with Scala.JS
      .style("fill", (_: js.Object, i: Int) => coloredUsers(i).fill)

    // Add a text label
    val groupText: Selection[Group] = group.append("text")
      .attr("x", 6)
      .attr("dy", 15)

    groupText.append("textPath")
      .attr("xlink:href", (d: js.Object, i: Int) => s"#${coloredUsers(i).id}" )
      .text((d: js.Object, i: Int) => coloredUsers(i).id)

    // Remove the labels that don't fit. :(
    // mg: cannot translate to Scala.JS
//    groupText.filter(function(d, i) { return groupPath[0][i].getTotalLength() / 2 - 16 < this.getComputedTextLength(); })
//      .remove();


    // Add the chords
    val chord = addData(layout.chords(), "chord", "path")(g)
      // Properly typed implementation does not work with d3js 0.3.4
      .style("fill", (d: Any, _: Int) => coloredUsers(d.asInstanceOf[Link[Node]].source.index.toInt).fill)
//      .attr("d", path) // mg: does not compile with Scala.JS

    // Add an elaborate mouseover title for each chord.
    chord.append("title").text((d: Any, _: Int) => {
      val l = d.asInstanceOf[Link[Node]]
      s"${coloredUsers(l.source.index.toInt).name} -> ${coloredUsers(l.target.index.toInt).name}: ${l.target.value}\n" +
        s"${coloredUsers(l.target.index.toInt).name} -> ${coloredUsers(l.source.index.toInt).name}: ${l.source.value}"
    })

    g.on("mouseleave", (_, _, _) => chord.classed("fade", false))

    // does not even work with type casts in d3js 0.3.4
//    group.on("mouseover", (_, i, _) => chord.classed("fade",
//      (l: Link[Node], _:, _:) => l.source.index != i && l.target.index != i)
//    )

    Graph(users, arc, path, group, groupPath, groupText, chord)
  }
}
