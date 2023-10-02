package id.bts.route

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun Application.configureWebSocketRouting() {
  routing {
    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    webSocket("/chat/") {
      println("Adding user!")
      val thisConnection = Connection(this)
      connections += thisConnection
      try {
        connections.forEach {
          it.session.send("You are connected! There are ${connections.count()} users here.")
        }
        for (frame in incoming) {
          frame as? Frame.Text ?: continue
          val receivedText = frame.readText()
          val textWithUsername = "[${thisConnection.name}]: $receivedText"
          connections.forEach {
            if (it.session != this) {
              it.session.send(textWithUsername)
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        println("error: " + e.localizedMessage)
      } finally {
        println("Removing $thisConnection!")
        connections -= thisConnection
      }
    }
  }
}

class Connection(val session: DefaultWebSocketSession) {
  companion object {
    val lastId = AtomicInteger(0)
  }

  val name = "user${lastId.getAndIncrement()}"
}