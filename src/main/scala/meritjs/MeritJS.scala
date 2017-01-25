package meritjs

import meritjs.layouts.Force

import scala.scalajs.js
import scala.scalajs.js.Array
import org.singlespaced.d3js.d3

import scala.scalajs.js.annotation.JSExport

/**
  * Created by gante on 19.01.17.
  */
object MeritJS extends js.JSApp with Json {
  var config: Config = _
  var onReady: Option[String] = None
  var merits: Array[JSMeritNode] = _
  var trx: Array[JSMeritLink] = _

  def main(): Unit = {
    getJson("config.json", configCallback)
  }

  @JSExport
  def draw(graphType: String): Unit = {
    if(trx == null) {
      println("setting onReady")
      onReady = Option(graphType)
    }
    else {
      val svg = d3.select("#graph")
      svg.selectAll("*").remove()
      svg.attr("width", config.graph_width)
      svg.attr("height", config.graph_height)
      graphType match {
        case "force" => new Force(svg, merits, trx, config.graph_width, config.graph_height)
        case "force-split" => new Force(svg, merits, trx, config.graph_width, config.graph_height, true)
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
    getJson(trxUri, trxCallback)
  }

  private def trxCallback(trxJson: Array[JSMeritLink]) = {
    trx = trxJson

    onReady match {
      case None => {}
      case Some(g) => draw(g)
    }
  }
}
