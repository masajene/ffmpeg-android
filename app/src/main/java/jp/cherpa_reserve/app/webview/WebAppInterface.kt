package jp.cherpa_reserve.app.webview

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun playAnnounce(message: String): Boolean {
        isValidNumber(message).let {
            if (it) {
                val audio = AudioCreater(context)
                audio.combineAndPlayAudio(message)
            } else {
                Toast.makeText(context, "不正な値です", Toast.LENGTH_SHORT).show()
            }
            return it
        }
    }

    private fun isValidNumber(number: String): Boolean {
        return number.matches(Regex("^[0-9]{1,4}$"))
    }
}