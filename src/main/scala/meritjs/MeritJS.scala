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

  def main(): Unit = {
    getJson("config.json", configCallback)
  }

  @JSExport
  def draw(graphType: String): Graph = {
    implicit val svg = d3.select("#graph")
    if(trx == null) {
      import scala.scalajs.js.timers._

      setTimeout(1000) {
         js.eval(s"draw('$graphType')")
      }
      Graph(merits)
    }
    else {
      svg.selectAll("*").remove()
      svg.attr("width", config.graph_width)
      svg.attr("height", config.graph_height)
      graphType match {
        case "force" =>
          Force.draw(merits, trx, config.graph_width, config.graph_height)
        case "force-split" =>
          Force.draw(merits, trx, config.graph_width, config.graph_height, true)
        case "chord" =>
          Chord.draw(merits, trx, config.graph_width, config.graph_height)
      }
    }
  }

  private def configCallback(cfg: Config) {
    val trxUri = s"${cfg.baseUrl}/${cfg.version}/${cfg.transactions}"
    val mrtUri = s"${cfg.baseUrl}/${cfg.version}/${cfg.merits}"
    getJson(mrtUri, meritCallback(trxUri))
    config = cfg
  }

  private def meritCallback(trxUri: String)(nodesJson: Array[JSMeritNode]): Unit = {
    merits = nodesJson
    getJson(trxUri + "?booked=false", trxCallback)
  }

  private def trxCallback(trxJson: Array[JSMeritLink]) = {
    trx = trxJson
  }
}
