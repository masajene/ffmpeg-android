package jp.cherpa_reserve.app.webview

import android.content.Intent
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import jp.cherpa_reserve.app.webview.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var kioskUtils: KioskUtils

    private var isLeftEdgeTouched = false
    private var rightEdgeTapCount = 0
    private val timeoutHandler = Handler()
    private var timeoutRunnable: Runnable? = null

    companion object {
        private const val SOME_THRESHOLD = 300 // 画面端の閾値
        private const val LONG_PRESS_TIME = 500 // ロングプレスとして認識する時間 (ミリ秒)
        private const val isRelease = true
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
        if (isRelease) {
            binding.view3.visibility = View.GONE
            binding.view4.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        setupWebView()
    }

    private fun setupWebView() {
        val url = SharedPref().getKey("url")
        binding.webview.apply {
            val settings: WebSettings = settings
            settings.domStorageEnabled = true
            val webAppInterface = WebAppInterface(this@MainActivity)
            addJavascriptInterface(webAppInterface, "Android")
            webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler,
                    error: SslError
                ) {
                    handler.proceed()
                }
            }
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
                        binding.view3.setBackgroundColor(getColor(R.color.purple_500))
                        setupTimeout()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isLeftEdgeTouched && x > (v.width - SOME_THRESHOLD)) {
                        rightEdgeTapCount++
                        if (!isRelease) {
                            binding.view4.run {
                                visibility = View.GONE
                                postDelayed({
                                    visibility = View.VISIBLE
                                }, 100)
                            }
                        }
//                        Toast.makeText(this, "$rightEdgeTapCount", Toast.LENGTH_SHORT).show()
                        if (rightEdgeTapCount == 5) {
                            timeoutRunnable?.let { timeoutHandler.removeCallbacks(it) }
                            binding.view3.setBackgroundColor(Color.parseColor("#ff1111"))
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
            binding.view3.setBackgroundColor(Color.parseColor("#ff1111"))
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