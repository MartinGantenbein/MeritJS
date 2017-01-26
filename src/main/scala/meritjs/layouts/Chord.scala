package meritjs.layouts

import meritjs.{Graph, JSMeritLink, JSMeritNode}
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.{Selection, d3}

import scala.scalajs.js.Array

/**
  * Created by mg on 26.01.2017.
  */
object Chord {
  def draw(svg: Selection[EventTarget], users: Array[JSMeritNode], transactions: Array[JSMeritLink],
           width: Double, height: Double): Graph = {
    def getMerits(sender: JSMeritNode, receiver: JSMeritNode): Double = {
      transactions.filter(p => p.from == sender.userId && p.to == receiver.userId).map(_.amount).sum
    }

    Graph(svg, users, users.map(u => users.map(getMerits(_, u))))
  }
}
