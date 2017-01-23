package meritjs

import scala.scalajs.js
import scala.scalajs.js.Array
import org.singlespaced.d3js.{Link, d3}
import org.singlespaced.d3js.forceModule.{Force, Node}

/**
  * Created by gante on 19.01.17.
  */

class MeritNode(val id: String, val balance: Int, val fill: String = "#000") extends Node
class MeritLink(val source: MeritNode, val target: MeritNode, val amount: Double) extends Link[MeritNode]

object MeritJS extends js.JSApp with Json with ForceLayout {
  def main(): Unit = {
    getJson("config.json", configCallback)
  }

  private def configCallback(cfg: Config) {
    val usrUri = s"${cfg.baseUrl}/${cfg.version}/${cfg.users}"
    val trxUri = s"${cfg.baseUrl}/${cfg.version}/${cfg.transactions}"

    getJson(usrUri, nodeCallback(trxUri, linkCallback))
  }

  private def nodeCallback(linkUri: String, callback: (Array[MeritNode]) => (Array[JSMeritLink]) => Force[MeritNode, MeritLink])(nodesJson: Array[JSMeritNode]): Unit = {
    val nodes = nodesJson.map(n => new MeritNode(n.id, n.balance))
    getJson(linkUri, linkCallback(nodes))
  }

  private def linkCallback(nodes: Array[MeritNode])(linksJson: Array[JSMeritLink]) = {
    def getLinks(nodes: Array[MeritNode], senderPrefix: String = "", receiverPrefix: String = "") = {
      val nodeMap = (for (n <- nodes) yield (n.id, n)).toMap
      linksJson.filter((l) => nodeMap.contains(s"$senderPrefix${l.from}")
        && nodeMap.contains(s"$senderPrefix${l.to}"))
        .map(l => new MeritLink(
          nodeMap(s"$senderPrefix${l.from}"),
          nodeMap(s"$receiverPrefix${l.to}"),
          l.amount))
    }

    createForceLayout("force", nodes, getLinks(nodes)).start()

    val nodes2 = nodes.flatMap((n) => Array(
      new MeritNode(s"from: ${n.id}", n.balance, "#0f0"),
      new MeritNode(s"to: ${n.id}", n.balance, "#f00")))
    createForceLayout("force2", nodes2, getLinks(nodes2, "from: ", "to: ")).start()
  }
}
