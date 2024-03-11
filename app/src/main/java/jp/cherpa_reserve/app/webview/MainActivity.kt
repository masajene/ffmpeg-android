package jp.cherpa_reserve.app.webview

import android.content.Intent
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import jp.cherpa_reserve.app.webview.CustomWebView.OnTouchEventCallback
import jp.cherpa_reserve.app.webview.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var kioskUtils: KioskUtils
    private lateinit var noOperationTimeoutTime: Number

    private var isLeftEdgeTouched = false
    private var rightEdgeTapCount = 0
    private val easterEggTimeoutHandler = Handler(Looper.getMainLooper())
    private var easterEggTimeoutRunnable: Runnable? = null
    private val noOperationTimeoutHandler = Handler(Looper.getMainLooper())
    private var noOperationTimeoutRunnable: Runnable? = null

    companion object {
        private const val SOME_THRESHOLD = 300 // 画面端の閾値
        private const val LONG_PRESS_TIME = 500 // ロングプレスとして認識する時間 (ミリ秒)
        private const val isRelease = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        noOperationTimeoutTime = if (SharedPref().getLong("noOperationTimeoutTime") == 0L) 1000 * 60 else SharedPref().getLong("noOperationTimeoutTime")

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
        noOperationTimeoutTime = if (SharedPref().getLong("noOperationTimeoutTime") == 0L) 1000 * 60 else SharedPref().getLong("noOperationTimeoutTime")
        setupWebView()
    }

    private fun setupWebView() {
        setupNoOperationTimeout()
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
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.swiper.isRefreshing = false
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
        binding.webview.setOnTouchEventCallback(object : OnTouchEventCallback {
            override fun onTouchStateChanged(state: Int) {
                println("onTouchStateChanged: $state")
                if (state == MotionEvent.ACTION_UP || state == MotionEvent.ACTION_CANCEL) {
                    setupNoOperationTimeout()
                }
            }
        })
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
                            easterEggTimeoutRunnable?.let { easterEggTimeoutHandler.removeCallbacks(it) }
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

        /// WebViewのスワイプ更新
        binding.swiper.setOnRefreshListener {
            binding.webview.reload();
        };
        binding.swiper.viewTreeObserver.addOnScrollChangedListener {
            if (binding.webview.scrollY == 0)
                binding.swiper.setEnabled(true);
            else
                binding.swiper.setEnabled(false);
        }
    }

    private fun setupTimeout() {
        easterEggTimeoutRunnable = Runnable {
            isLeftEdgeTouched = false
            rightEdgeTapCount = 0
            binding.view3.setBackgroundColor(Color.parseColor("#ff1111"))
        }.also {
            easterEggTimeoutHandler.postDelayed(it, 10000)
        }
    }

    private fun setupNoOperationTimeout() {
        println("setupNoOperationTimeout!")
        noOperationTimeoutRunnable?.let { noOperationTimeoutHandler.removeCallbacks(it) }
        noOperationTimeoutRunnable = Runnable {
            println("no operation timeout!")
            binding.webview.reload()
            setupNoOperationTimeout()
        }.also {
            noOperationTimeoutHandler.postDelayed(it, noOperationTimeoutTime.toLong())
        }
    }

    private fun launchHiddenActivity() {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
        rightEdgeTapCount = 0
        noOperationTimeoutRunnable?.let { noOperationTimeoutHandler.removeCallbacks(it) }
    }
}