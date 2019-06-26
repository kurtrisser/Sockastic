package com.someobscure.sockastic

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.Runnable
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//TODO: Provide small video showing typical usage.

const val LOCAL_SERVER_IP = "127.0.0.1"
const val LOCAL_SERVER_PORT = 9985
const val DRAWABLE_BOTTOM = 3

class MainActivity : AppCompatActivity() {
  var serverIsRunning = false
  private val help = Help(this)
  private var clientMessages = StringBuilder()
  private var serverMessages = StringBuilder()
  private val localClient = SocketClient(clientMessages)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    prepareSockasticTextView(serverTextView)
    prepareSockasticTextView(clientTextView)

    help.setTextViews(clientTextView, serverTextView)
    /* -requestText- */
    prepareRequestEditText()
    /* -requestIp- */
    prepareRequestIpEditText()
    /* -requestPort- */
    prepareRequestPortEditText()

    help.showHelp()

  } // End of onCreate()

  private fun prepareSockasticTextView(textView: TextView) {
    textView.movementMethod = ScrollingMovementMethod()
    registerForContextMenu(textView)
    textView.setOnTouchListener { v, event ->
      val action = event.action
      when (action) {
        MotionEvent.ACTION_DOWN ->
          // Disallow ScrollView to intercept touch events.
          outerScrollView.requestDisallowInterceptTouchEvent(true)

        MotionEvent.ACTION_UP ->
          // Allow ScrollView to intercept touch events.
          outerScrollView.requestDisallowInterceptTouchEvent(false)
      }
      false
    }
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    println("onRestoreInstanceState(${savedInstanceState.toString()}): ")
    serverTextView.text = savedInstanceState?.getCharSequence("serverTextView")
    clientTextView.text = savedInstanceState?.getCharSequence("clientTextView")
    serverIsRunning = savedInstanceState?.getBoolean("serverIsRunning") ?: false
  }

  public override fun onResume() {
    this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    Companion.chooseFocus(this)
    println("onResume() ")
    super.onResume()
  }

  public override fun onStart() {
    println("onStart() ")
    this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    super.onStart()
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)
    println("onSaveInstanceState() ")
    outState?.putBoolean("serverIsRunning", serverIsRunning)
    outState?.putCharSequence("serverTextView", serverTextView.text)
    outState?.putCharSequence("clientTextView", clientTextView.text)
  }

  override fun onPause() {
    println("onPause() ")
    super.onPause()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.options_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_help -> {
        help.showHelpDialog()
        true
      }
      R.id.action_about -> {
        showAbout()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun showAbout() {
    help.showAboutDialog()
  }

  override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
    super.onCreateContextMenu(menu, v, menuInfo)
    val inflater = menuInflater
    when (v?.id) {
      serverTextView.id -> inflater.inflate(R.menu.context_server, menu)
      clientTextView.id -> inflater.inflate(R.menu.context_client, menu)
    }

  }

  override fun onContextItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_clear_exercise -> {
        clientMessages.clear()
        clientTextView.text = ""
        true
      }
      R.id.action_clear_server -> {
        serverMessages.clear()
        serverTextView.text = ""
        true
      }
      else ->
        super.onContextItemSelected(item)
    }
  }

  fun onServerButtonClick(view: View) {
    println("onServerButtonClick():  ")
    val serverRunningAnimation: AnimationDrawable =
      view.btn_server.compoundDrawables[DRAWABLE_BOTTOM] as AnimationDrawable
    if (serverIsRunning) {  // STOP the server.
      btn_server.text = view.resources.getString(R.string.start_server)
      /*btn_exercise.isEnabled = usesDefaultEndpoint(localClient).not()*/
      btn_connect_client.isEnabled = Companion.usesDefaultEndpoint(this, localClient).not()
      hardShutdown.visibility = View.GONE
      /* Stop the Server. */
      serverStop(server, hardShutdown.isChecked)
      serverRunningAnimation.stop()
      serverIsRunning = false
      if (usesDefaultEndpoint(this, localClient) && Companion.isLocalClientOpen(this) && hardShutdown.isChecked) {
        /* If the local client is connected to the local Socket Server that was being stopped with hardShutdown=true,
           then cleanly disconnect the local client also.  Calling onClientConnect() here will close the client. */
        onClientConnect(view)
      }
      //      btn_send_receive.isEnabled = Companion.isLocalClientOpen(this) && (requestText.text.isNotEmpty()) && usesDefaultEndpoint(this,localClient).not()
    } else { // START the server.
      btn_server.text = view.resources.getString(R.string.stop_server)
      /*btn_exercise.isEnabled = usesDefaultEndpoint(localClient)*/
      btn_connect_client.isEnabled = true
      btn_send_receive.isEnabled = Companion.isLocalClientOpen(this) && (requestText.text.isNotEmpty())
      serverRunningAnimation.start()
      hardShutdown.isChecked = true
      hardShutdown.visibility = View.VISIBLE
      /* Start server in a separate Thread so it can keep accepting new connections. */
      Thread(Runnable {
        serverStart(view.context, serverTextView, serverMessages)
      }).start()
      serverIsRunning = true
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
      var hasConsumed = false
      if (v is EditText) {
        if (event.x >= v.width - v.totalPaddingRight) {
          if (event.action == MotionEvent.ACTION_UP) {
            onClicked(this)
          }
          hasConsumed = true
        }
      }
      hasConsumed
    }
  }

  private fun prepareRequestPortEditText() {
    requestPort.onRightDrawableClicked { it.text.clear() }
    requestPort.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, len: Int) {
        requestPort.setCompoundDrawablesWithIntrinsicBounds(
          0,
          0,
          if (requestPort.text.isNotEmpty()) R.drawable.ic_cancel_gray_24dp else 0,
          0
        )
        requestPort.requestFocus()
        btn_connect_client.isEnabled =
          Companion.isLocalClientOpen(this@MainActivity).not() && ((requestPort.text.isNotEmpty()) || (serverIsRunning))
      }
    })
  }

  private fun prepareRequestIpEditText() {
    requestIp.onRightDrawableClicked { it.text.clear() }
    /* setRawInputType selects numeric keypad without disallowing multiple decimal points. */
    requestIp.setRawInputType(InputType.TYPE_CLASS_NUMBER)
    requestIp.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, len: Int) {
        requestIp.setCompoundDrawablesWithIntrinsicBounds(
          0, 0,
          if (requestIp.text.isNotEmpty()) R.drawable.ic_cancel_gray_24dp else 0, 0
        )
        requestIp.requestFocus()
        // CONNECT is enabled if local client is NOT active, and EITHER requestIp is provided OR local server is running.
        btn_connect_client.isEnabled =
          Companion.isLocalClientOpen(this@MainActivity).not() && ((requestIp.text.isNotEmpty()) || (serverIsRunning))
      }
    })
  }

  private fun prepareRequestEditText() {
    requestText.onRightDrawableClicked { it.text.clear() }
    requestText.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, len: Int) {
        println("requestText.onTextChanged('$p0, $p1, $p2, $len') ")
        requestText.setCompoundDrawablesWithIntrinsicBounds(
          0, 0, if (requestText.text.isNotEmpty()) R.drawable.ic_cancel_gray_24dp else 0, 0
        )
        requestText.requestFocus()
        btn_send_receive.isEnabled = Companion.isLocalClientOpen(this@MainActivity) && (requestText.text.isNotEmpty())
      }
    })
  }

  fun onClientExercise(view: View) {
    Thread(Runnable {
      exerciseServer(clientTextView)
    }).start()
  }

  fun onClientConnect(view: View) {
    Thread(Runnable {
      /* TOGGLE CONNECT/DISCONNECT */
      if (Companion.isLocalClientOpen(this)) {  // If localClient is connected and open
        clientMessages.appendln(disconnectFromServer(localClient))  // DISCONNECT
      } else {  // If localClient is disconnected, i.e., not both connected and open.
        localClient.connectToServer(targetIP(localClient), targetPort(localClient))
      }
      writeMsgOnUiThread(clientTextView, clientMessages.toString())
    }).start()
    Thread.sleep(200) // Short pause to allow previous socket operation to settle before calling isLocalClientOpen().
    btn_connect_client.text =
      this.resources.getString(if (Companion.isLocalClientOpen(this)) R.string.disconnect else R.string.connect)
    requestText.isEnabled = Companion.isLocalClientOpen(this)
    requestIp.isEnabled = Companion.isLocalClientOpen(this).not()
    requestPort.isEnabled = Companion.isLocalClientOpen(this).not()
    btn_send_receive.isEnabled = Companion.isLocalClientOpen(this) && (requestText.text.isNotEmpty())
    Companion.chooseFocus(this)
  }

  override fun finish() {
    println("FINISH - Bye! ")
    if (Companion.isLocalClientOpen(this)) localClient.stopConnection()
    if (serverIsRunning) server.close()
    super.finish()
  }

  private fun targetPort(sockCli: SocketClient): Int {
    return if (requestPort.text.trim().isNotEmpty()) requestPort.text.toString().trim().toInt() else sockCli.defaultSvrPort
  }

  private fun targetIP(sockCli: SocketClient): String {
    return if (requestIp.text.trim().isNotEmpty()) requestIp.text.toString().trim() else sockCli.defaultSvrIp
  }

  @Throws(IllegalStateException::class)
  fun converseWithServer(request: String, client: SocketClient): String {
    let {
      val reply = try {
        sendReceive(client, request)
      } catch (isex: Exception) {
        println("converseWithServer(): Exception $isex")
        ""
      }
      writeMsgOnUiThread(clientTextView, clientMessages.toString())
      return reply
    }
  }

  private fun exerciseServer(view: TextView) {
    println("exerciseServer(): ")
    writeMsgOnUiThread(view, "exerciseServer(): ")

    val client1 = SocketClient(clientMessages)
    clientMessages.append(client1.connectToServer(targetIP(client1), targetPort(client1)))

    try {
      if (requestText.text.isNullOrBlank().not()) {
        converseWithServer("status", client1)
        converseWithServer("Os", client1)
        converseWithServer("status", client1)
        converseWithServer("Au", client1)
        converseWithServer("Fluorine", client1)
        converseWithServer("47", client1)
        converseWithServer("Beryllium", client1)
        converseWithServer("STATUS", client1)
        converseWithServer("Neon", client1)
        converseWithServer("STATUS", client1)
        clientMessages.appendln(disconnectFromServer(client1))
      }
    } catch (ex: Exception) {
      println("exerciseServer(): ex=$ex")
    } finally {
      clientMessages.appendln(disconnectFromServer(client1))
      writeMsgOnUiThread(view, clientMessages.toString())
    }
  }

  private fun sendReceive(
    client: SocketClient,
    request: String
  ): String {
    return client.sendMessage(content = request)
  }

  /** disconnectFromServer() is called from the CLIENT */
  private fun disconnectFromServer(client: SocketClient): String {
    var response = ""
    val soughtTag: String = if (serverClientSessions.size > 0) serverClientSessions.last().tag else "x"
    try {
      println("disconnectFromServer(): client$soughtTag of ${serverClientSessions.size} clients. ")
      client.stopConnection()
      response = "disconnected at ${timeNow}"
      if (serverClientSessions.size > 0) {
        /* Remove from the list of clients */
        println("disconnectFromServer(): calling remove() for element tagged '$soughtTag' of ${serverClientSessions.size} clients. ")
        serverClientSessions.removeIf { scs -> scs.tag == soughtTag } // ve(serverClientSessions.last())
      }
    } catch (e: IOException) {
      response = "could not disconnect client '$soughtTag'"
      e.printStackTrace()
    }
    return response
  }

  private fun writeMsgOnUiThread(view: TextView, msg: String) {
    Thread(Runnable
    {
      this@MainActivity.runOnUiThread {
        view.text = msg
      }
    }).start()
  }

  companion object {
    private fun isLocalClientOpen(mainActivity: MainActivity): Boolean {
      val lco = (mainActivity.localClient.socket?.isConnected
        ?: false) && mainActivity.localClient.socket?.isClosed?.not() ?: false
      println("isLocalClientOpen(): returning $lco.")
      return lco
    }

    private fun usesDefaultEndpoint(mainActivity: MainActivity, sockCli: SocketClient): Boolean {
      return (sockCli.defaultSvrIp == mainActivity.targetIP(sockCli) &&
           sockCli.defaultSvrPort == mainActivity.targetPort(sockCli))
    }

    private fun chooseFocus(mainActivity: MainActivity) {
      if (isLocalClientOpen(mainActivity)) mainActivity.requestText.requestFocus() else mainActivity.requestIp.requestFocus()
    }

  } // End of Companion object declaration.

  fun onClientSend(view: View) {
    Thread(Runnable {
      converseWithServer(requestText.text.toString().trim(), localClient)
    }).start()
  }
} // End of MainActivity

val timeNow: String?
  get() {
    return try {
      SimpleDateFormat("HH:mm:ss z").format(Date(System.currentTimeMillis()))
    } catch (e: Exception) {
      println("Unexpected exception during timeNow(): ${e.message}")
      e.toString()
    }
  }