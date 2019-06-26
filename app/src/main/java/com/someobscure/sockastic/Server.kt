package com.someobscure.sockastic

//https://github.com/sht5/Android-tcp-server-and-client.git
//package dk.im2b
//https://gist.github.com/Silverbaq/a14fe6b3ec57703e8cc1a63b59605876

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.widget.TextView
import java.math.BigInteger
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteOrder
import java.util.*
import java.util.Collections.synchronizedList
import kotlin.concurrent.thread

val serverClientSessions: MutableList<ServerClientSession> = synchronizedList(ArrayList<ServerClientSession>())

var serving = true
var server = ServerSocket(localServerPort)
var localWiFiAddr: String = "ipAddr:port"

fun serverStart(context: Context, view: TextView, serverMessages: StringBuilder) {
  serverMessages.clear()
  if (server.isClosed) {
    server = ServerSocket(localServerPort)
  }
  serving = true
  localWiFiAddr = obtainWifiIpAddress(context) + ":" + server.localPort
  serverMessages.appendln("[${timeNow}] Server now listening on $localWiFiAddr.")
  showMessageOnTextView(view, serverMessages.toString())

  while (serving) {
    try {
      val client = server.accept()
      serverMessages.appendln("[${timeNow}] Client connection accepted from ${client.inetAddress.hostAddress}. ")
      showMessageOnTextView(view, serverMessages.toString())

      // Execute each ServerClientSession in a new, separate thread. */
      val svrClientSession = ServerClientSession(client, context, view, serverMessages, "${serverClientSessions.size}")
      serverClientSessions.add(svrClientSession)
      // --------------------------------------------- */
      // --------------------------------------------- */
      thread { svrClientSession.run() }
      // --------------------------------------------- */
      // --------------------------------------------- */
    } catch (se: SocketException) {
      println("SVR: Server will shutdown. ")
      serverMessages.appendln("SVR: Server.serverStart(): Server will shutdown. ")
      break
    }
  }
  serverMessages.appendln(serverStop(server, false))
  showMessageOnTextView(view, serverMessages.toString())
}

/**
 * serverStop
 * Stops running ServerSocket, and if hard is true, disconnects any connected clients.
 * Otherwise continues to serve existing connected clients while not accepting new socket connections.
 * @param server the active ServerSocket instance to stop.
 * @param hard if true, immediately disconnect any client connections.
 * @return a String containing user status messages.
 */
fun serverStop(server: ServerSocket, hard: Boolean): String {
  println("serverStop() ")
  serving = false
  val statusMsg = StringBuilder()
  server.close()
  val remainingClients = serverClientSessions.size
  statusMsg.append("[${timeNow}] Server stopped listening on $localWiFiAddr")
  if (remainingClients > 0)
    statusMsg.appendln(", and will terminate as $remainingClients remaining clients disconnect. ")
  else
    statusMsg.appendln(". ")
  println(statusMsg)
  if (hard && (remainingClients > 0)) {
    serverClientSessions.forEach { session ->
      if (session.client.isClosed.not()) {
        println("SVR: serverStop(): closing session ${session.tag}... ")
        session.client.close()
      } else {
        println("SVR: serverStop(): session ${session.tag} is already closed... ")
      }
    }
    statusMsg.appendln(" Server stopped after $remainingClients remaining clients disconnected. ")
    println(statusMsg.toString())
    serverClientSessions.clear()
  }
  /* Remove from our list of clients */

  return statusMsg.toString()
}

fun showMessageOnTextView(view: TextView, msg: String) {
  view.handler.post(Runnable { view.text = msg })
}

/* End of original Server.kt      */

private fun obtainWifiIpAddress(context: Context): String? {
  val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
  var ip = wifiManager.connectionInfo.ipAddress

  if (ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN) ip = Integer.reverseBytes(ip)

//  val ipByteArray = BigInteger.valueOf(ip.toLong()).toByteArray()

  lateinit var ipString: String
  try {
    ipString = InetAddress.getByAddress(BigInteger.valueOf(ip.toLong()).toByteArray()).hostAddress
  } catch (ex: UnknownHostException) {
    ipString = "${ex.message}"
  } finally {
    if (ipString.isNullOrEmpty()) ipString = ""
  }

  return ipString
}
