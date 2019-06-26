package com.someobscure.sockastic

import android.content.Context
import android.widget.TextView
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

/**
 * A ServerClientSession runs on a separate thread and handles each client connection accepted by server.
 */
class ServerClientSession(
  var client: Socket,
  private val context: Context,
  private val view: TextView,
  private val serverMessages: StringBuilder,
  var tag: String = ""
) {
  //  val client: Socket = client
  private val reader: Scanner = Scanner(client.getInputStream())
  private val writer: OutputStream = client.getOutputStream()
  private val mendeleev: Mendeleev = Mendeleev()
  private var running: Boolean = false

  fun run() {
    running = true
    while (running) {
      try {
        val text = reader.nextLine()
        if (text.trim().toUpperCase() == "HELP" || text.trim() == "?") {
          write("${mendeleev.usage()} ")
          continue
        }
        if (text.trim().toUpperCase() == "EXIT") {
          serverMessages.appendln("EXIT")
          disconnectClientHandler(context, view)
          serverMessages.appendln("${client.inetAddress.hostAddress} disconnected. ")
          showMessageOnTextView(view, serverMessages.toString())
          continue
        }
        val values = text.split(' ')
        val STATUS = "STATUS"
        var result =
          if (values.size != 1) {
            "Must provide 1 word in request, 'He', '2', or 'Helium', etc., or '$STATUS'. "
          } else {
            if (values[0].trim().toUpperCase() == STATUS)
              "Active client count: ${serverClientSessions.size}"
            else
              mendeleev.lookup(text)
          }
        write(result)
        serverMessages.appendln("Serviced request [$text], reply [$result] sent. ")
      } catch (ex: Exception) {
        disconnectClientHandler(context, view)
      } finally {
        showMessageOnTextView(view, serverMessages.toString())
      }
    }
  }

  private fun write(message: String) {
    writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
  }

  /** disconnectClientHandler() is called from the SERVER client session handler.  */
  private fun disconnectClientHandler(context: Context, view: TextView) {
    /* disconnectClientHandler this client handler. */
    client.close()
    running = false
    val lastTag = this.tag
    val removed = serverClientSessions.removeIf { scs -> scs.tag == lastTag }
    if (removed) {
      println("SCS: disconnectClientHandler(): Removed serverClientSession '$lastTag'")
      serverMessages.appendln("Client '$lastTag' connection at ${client.inetAddress.hostAddress} disconnected.  ${serverClientSessions.size} clients remain. ")
    } else {
      println("SCS: disconnectClientHandler(): UNEXPECTED ERROR: could not remove client '$lastTag' session.  ${serverClientSessions.size} clients remain.")
    }
    showMessageOnTextView(view, serverMessages.toString())
  }

}