package meritjs

import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.{Selection, d3}

import scala.scalajs.js
import scala.scalajs.js.Array

/**
  * Created by gante on 23.01.17.
  */
@js.native
trait JSMeritNode extends js.Object {
  val userId: String=js.native
  val name: String=js.native
  val sent: Int=js.native
  val received: Int=js.native
}

@js.native
trait JSMeritLink extends js.Object {
  val from:   String=js.native
  val to:     String=js.native
  val amount: Int=js.native
  val booked: Boolean=js.native
}

@js.native
trait Graph extends js.Object {
  val svg: js.Object = js.native
  val arc: js.Object = js.native
  val path: js.Object = js.native
  val merits: Array[JSMeritNode] = js.native
  val group: js.Object = js.native
  val groupPath: js.Object = js.native
  val groupText: js.Object = js.native
  val chord: js.Object = js.native
}

object Graph {
  def apply(merits: Array[JSMeritNode], arc: js.Object = null, path: js.Object = null, group: js.Object = null,
            groupPath: js.Object = null, groupText: js.Object = null, chord: js.Object = null)(implicit svg: Selection[EventTarget]): Graph = {
    js.Dynamic.literal(svg = svg, merits = merits, arc = arc, path = path, group = group,
      groupPath = groupPath, groupText = groupText, chord = chord).asInstanceOf[Graph]
  }
}

@js.native
trait Config extends js.Object {
  val baseUrl: String=js.native
  val team_auth: String=js.native
}

trait Json {
  protected def getJson[T](uri: String, callback: (T) => Unit): Unit = {
    d3.json(uri, (error: js.Any, json: js.Any) => {
      if (error != null) {
        js.Dynamic.global.console.error(error)
      }
      else {
        callback(json.asInstanceOf[T])
      }
    })
  }
}
