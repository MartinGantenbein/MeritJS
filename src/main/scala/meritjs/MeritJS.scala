package meritjs

import meritjs.layouts.{Chord, Force}

import scala.scalajs.js
import scala.scalajs.js.Array
import org.singlespaced.d3js.d3

import scala.scalajs.js.annotation.JSExport

/**
  * Created by gante on 19.01.17.
  */
object MeritJS extends js.JSApp with Json {
  var config: Config = _
  var merits: Array[JSMeritNode] = _
  var trx: Array[JSMeritLink] = _

  def main(): Unit = {}

  @JSExport
  def init(cfg: Config) = {
    val trxUri = s"${cfg.baseUrl}/v1/transactions?auth=${cfg.team_auth}"
    val mrtUri = s"${cfg.baseUrl}/v1/merits/t3?auth=${cfg.team_auth}"
    getJson(mrtUri, meritCallback(trxUri))
    config = cfg
  }

  @JSExport
  def draw(graphType: String): Graph = {
    def getMerits(sender: JSMeritNode, recipient: JSMeritNode, senderPrefix: String = "", recipientPrefix: String = ""): (Double, String) = {
      val merits = trx.filter(p => s"$senderPrefix${p.from}" == sender.userId && s"$recipientPrefix${p.to}" == recipient.userId).map(_.amount).sum
      (merits, s"${sender.name} -> ${recipient.name}: $merits")
    }
    def sender(node: JSMeritNode): JSMeritNode = JSMeritNode(s"from: ${node.userId}", node.name, sent = node.sent)
    def recipient(node: JSMeritNode): JSMeritNode = JSMeritNode(s"to: ${node.userId}", node.name, received = node.received)

    implicit val svg = d3.select("#graph")
    if(trx == null) {
      import scala.scalajs.js.timers._

      setTimeout(1000) {
         js.eval(s"draw('$graphType')")
      }
      Graph()
    }
    else {
      svg.selectAll("*").remove()

      graphType match {
        case "force" => Force.draw(merits, trx)
        case "force-split" => Force.draw(merits, trx, split = true)
        case "chord-recv" =>  Chord.draw(merits, merits.map(u => merits.map(getMerits(_, u))))
        case "chord-send" =>  Chord.draw(merits, merits.map(u => merits.map(getMerits(u, _))))
        case "chord-both" =>
          val both: Array[JSMeritNode] = merits.map(sender) union merits.map(recipient)
          Chord.draw(both, both.map(u => both.map {
            if (u.userId.startsWith("to: ")) getMerits(_, u, senderPrefix = "from: ", recipientPrefix = "to: ")
            else getMerits(u, _, senderPrefix = "from: ", recipientPrefix = "to: ").copy(_2 = "")
          }))
      }
    }
  }

  private def meritCallback(trxUri: String)(nodesJson: Array[JSMeritNode]): Unit = {
    merits = nodesJson
    getJson(trxUri + "&booked=false", trxCallback)
  }

  private def trxCallback(trxJson: Array[JSMeritLink]) = {
    trx = trxJson
  }
}
