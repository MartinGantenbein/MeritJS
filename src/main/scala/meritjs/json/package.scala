package meritjs

import org.singlespaced.d3js.d3

import scala.scalajs.js

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
trait Config extends js.Object {
  val baseUrl: String=js.native
  val version: String=js.native
  val users: String=js.native
  val transactions: String=js.native
  val merits: String=js.native
  val graph_width: Double=js.native
  val graph_height: Double=js.native
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
