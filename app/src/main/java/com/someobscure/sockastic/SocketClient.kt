package com.someobscure.sockastic

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ConnectException
import java.net.Socket

class SocketClient(private val msgBuilder: StringBuilder) {
  var socket: Socket? = null

  val defaultSvrIp = LOCAL_SERVER_IP
  val defaultSvrPort = LOCAL_SERVER_PORT
  private var ip: String = defaultSvrIp
  private var port: Int = defaultSvrPort
  private var writer: PrintWriter? = null
  private var reader: BufferedReader? = null

  @Throws(IOException::class)
  fun connectToServer(svrIp: String = defaultSvrIp, svrPort: Int = defaultSvrPort): StringBuilder {
    ip = svrIp
    port = svrPort
    println("connectToServer($ip, $port): ")
    try {
      socket = Socket(ip, port)
    } catch (ctex: ConnectException) {
      msgBuilder.append("[${timeNow}] Connection timed writer on $ip:$port. ")
      return msgBuilder
    }
    println("connectToServer($ip, $port): socket=$socket")

    socket?.setSoLinger(true, 3)

    msgBuilder.append("[${timeNow}] Socket opened on $ip:$port. ")
    writer = PrintWriter(socket!!.getOutputStream(), true)
    reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
//        msgBuilder.append("IO streams connected. ")
    return msgBuilder
  }

  @Throws(IOException::class)
  fun sendMessage(content: String): String {
    print("sendMessage(\"$content\"): ")
    writer!!.println(content)
    msgBuilder.append(" sent:\"$content\"")
    print("sent: \"$content\" ")
    val reply = reader!!.readLine()
    println("received: \"$reply\"")
    msgBuilder.appendln(" received:\"$reply\" ")
    return reply
  }

  @Throws(IOException::class)
  fun stopConnection() {
    reader!!.close()
    writer!!.close()
    socket!!.close()
    msgBuilder.append(" *closed* ")
  }
}
