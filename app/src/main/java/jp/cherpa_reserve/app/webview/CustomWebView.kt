package jp.cherpa_reserve.app.webview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView


class CustomWebView : WebView {
    private var mOnTouchEventCallback: CustomWebView.OnTouchEventCallback? = null

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?) : super(context!!)

    fun setOnTouchEventCallback(onTouchEventCallback: OnTouchEventCallback) {
        mOnTouchEventCallback = onTouchEventCallback
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mOnTouchEventCallback?.onTouchStateChanged(event.action)
        return super.onTouchEvent(event)
    }

    interface OnTouchEventCallback {
        fun onTouchStateChanged(state: Int)
    }
}