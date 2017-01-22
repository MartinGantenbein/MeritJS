package meritjs

import scala.scalajs.js
import scala.scalajs.js.Array
import org.singlespaced.d3js.{Link, d3}
import org.singlespaced.d3js.forceModule.{Force, Node}

/**
  * Created by gante on 19.01.17.
  */

@js.native
trait JSMeritNode extends js.Object {
  val id: String=js.native
  val balance: Int=js.native
}

@js.native
trait JSMeritLink extends js.Object {
  val from:   String=js.native
  val to:     String=js.native
  val amount: Int=js.native
}

class MeritNode(val id: String, val balance: Int) extends Node
class MeritLink(val source: MeritNode, val target: MeritNode, val amount: Double) extends Link[MeritNode]

object MeritJS extends js.JSApp with ForceLayout {
  def main(): Unit = {
    getJson("/v1/users", nodeCallback("/v1/transactions", linkCallback))
  }

  private def getJson[T](uri: String, callback: (T) => Unit): Unit = {
    d3.json(Configuration.baseUrl + uri, (error: js.Any, json: js.Any) => {
      if (error != null) {
        js.Dynamic.global.console.error(error)
      }
      else {
        callback(json.asInstanceOf[T])
      }
    })
  }

  private def nodeCallback(linkUri: String, callback: (Array[MeritNode]) => (Array[JSMeritLink]) => Force[MeritNode, MeritLink])(nodesJson: Array[JSMeritNode]): Unit = {
    val nodes = nodesJson.map(n => new MeritNode(n.id, n.balance))
    getJson(linkUri, linkCallback(nodes))
  }

  private def linkCallback(nodes: Array[MeritNode])(linksJson: Array[JSMeritLink]) = {
    val nodeSeq: Seq[(String, MeritNode)] = for (n <- nodes) yield (n.id, n)
    val nodeMap = nodeSeq.toMap
    val links = linksJson.filter((l) => nodeMap.contains(l.from) && nodeMap.contains(l.to))
      .map(l => new MeritLink(nodeMap(l.from), nodeMap(l.to), l.amount))

    createForceLayout(nodes, links).start()
  }
}
