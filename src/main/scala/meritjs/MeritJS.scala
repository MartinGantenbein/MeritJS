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
        case "force-split" => Force.draw(merits, trx, true)
        case "chord" => Chord.draw(merits, trx)
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
