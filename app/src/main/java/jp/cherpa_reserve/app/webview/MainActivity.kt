package jp.cherpa_reserve.app.webview

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.Window
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import jp.cherpa_reserve.app.webview.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var kioskUtils: KioskUtils

    private var isLeftEdgeTouched = false
    private var rightEdgeTapCount = 0
    private val timeoutHandler = Handler()
    private var timeoutRunnable: Runnable? = null

    companion object {
        private const val SOME_THRESHOLD = 100 // 画面端の閾値
        private const val LONG_PRESS_TIME = 500 // ロングプレスとして認識する時間 (ミリ秒)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        kioskUtils = KioskUtils(this)
        kioskUtils.start(this)

        setupWebView()
        setupGesture()
    }

    override fun onResume() {
        super.onResume()
        setupWebView()
    }

    private fun setupWebView() {
        val url = SharedPref().getKey("url")
        binding.webview.apply {
            val webAppInterface = WebAppInterface(this@MainActivity)
            addJavascriptInterface(webAppInterface, "Android")
            webViewClient = WebViewClient()
            settings.apply {
                allowFileAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                javaScriptEnabled = true
            }
            loadUrl(url.ifBlank { "file:///android_asset/start.html" })
        }
    }

    private fun setupGesture() {
        binding.view2.setOnTouchListener { v, event ->
            val x = event.x.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (x < SOME_THRESHOLD) {
                        isLeftEdgeTouched = true
                        setupTimeout()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isLeftEdgeTouched && x > (v.width - SOME_THRESHOLD)) {
                        rightEdgeTapCount++
                        if (rightEdgeTapCount == 10) {
                            timeoutRunnable?.let { timeoutHandler.removeCallbacks(it) }
                            launchHiddenActivity()
                        }
                    } else {
                        v.performClick()
                    }
                }
//                MotionEvent.ACTION_MOVE -> isLeftEdgeTouched = false
            }
            true
        }
    }

    private fun setupTimeout() {
        timeoutRunnable = Runnable {
            isLeftEdgeTouched = false
            rightEdgeTapCount = 0
        }.also {
            timeoutHandler.postDelayed(it, 10000)
        }
    }

    private fun launchHiddenActivity() {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
        rightEdgeTapCount = 0
    }
}