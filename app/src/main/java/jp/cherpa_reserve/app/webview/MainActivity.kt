package jp.cherpa_reserve.app.webview

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import jp.cherpa_reserve.app.webview.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
    }

    private fun setupWebView() {
        binding.webview.apply {
            val webAppInterface = WebAppInterface(this@MainActivity)
            addJavascriptInterface(webAppInterface, "Android")
            settings.apply {
                allowFileAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                javaScriptEnabled = true
            }
            loadUrl("file:///android_asset/start.html")
        }
    }
}