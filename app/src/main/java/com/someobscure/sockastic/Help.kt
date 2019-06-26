package com.someobscure.sockastic

import android.app.Dialog
import android.content.Context
import android.text.Html
import android.text.Html.fromHtml
import android.text.method.ScrollingMovementMethod
import android.view.Window
import android.widget.TextView


class Help(private val context: Context) {

  private lateinit var clientTextView: TextView
  private lateinit var serverTextView: TextView

  fun setTextViews(cTextView: TextView, sTextView: TextView) {
    clientTextView = cTextView
    serverTextView = sTextView
  }

  private fun showDialog(title: String, content: String) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS)
    dialog.setCancelable(true)
    dialog.setTitle(title)
    dialog.setContentView(R.layout.help)

    val body = dialog.findViewById(R.id.contentTextView) as TextView
    body.movementMethod = ScrollingMovementMethod()
    body.text = fromHtml(content, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    dialog.show()

  }

  fun showHelpDialog() {
    showDialog(context.getString(R.string.overview_title), helpContent())
  }

  fun showAboutDialog() {
    showDialog(context.getString(R.string.about_title), aboutContent())
  }

  fun showHelp() {
    showServerHelp()
    showClientHelp()
  }

  private fun showServerHelp() {
    if (serverTextView != null) {
      serverTextView.text = fromHtml(serverHelpContent(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
      serverTextView.post(Runnable() { serverTextView.scrollTo(0, 0) })
    }
  }

  private fun showClientHelp() {
    if (clientTextView != null) {
      clientTextView.text = fromHtml(clientHelpContent(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
      clientTextView.post(Runnable() { clientTextView.scrollTo(0, 0) })
    }
  }

  private fun helpContent(): String {
    return "${overviewHelpContent()}<p>${serverHelpContent()}<p>${clientHelpContent()})"
  }

  private fun overviewHelpContent(): String {
    return "${context.resources.getString(R.string.overview_title)}<p>${context.resources.getString(R.string.overview_all)}"
  }

  private fun serverHelpContent(): String {
    return "${context.resources.getString(R.string.server_title)}<p>${context.resources.getString(R.string.server_all)}"
  }

  private fun clientHelpContent(): String {
    return "${context.resources.getString(R.string.client_title)}<p>${context.resources.getString(R.string.client_all)}"
  }

  private fun aboutContent(): String {
    return "${context.resources.getString(R.string.about_title)}<p>${context.resources.getString(R.string.about_all)}"
  }

}
