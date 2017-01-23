package meritjs

import scala.scalajs.js
import scala.scalajs.js.Array
import org.singlespaced.d3js.{Link, d3}
import org.singlespaced.d3js.forceModule.{Force, Node}

/**
  * Created by gante on 19.01.17.
  */

class MeritNode(val id: String, val balance: Int) extends Node
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
    val nodeSeq: Seq[(String, MeritNode)] = for (n <- nodes) yield (n.id, n)
    val nodeMap = nodeSeq.toMap
    val links = linksJson.filter((l) => nodeMap.contains(l.from) && nodeMap.contains(l.to))
      .map(l => new MeritLink(nodeMap(l.from), nodeMap(l.to), l.amount))

    createForceLayout(nodes, links).start()
  }
}
