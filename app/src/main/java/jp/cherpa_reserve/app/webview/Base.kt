package jp.cherpa_reserve.app.webview

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.util.DisplayMetrics
import androidx.annotation.NonNull


object Base {
    private var context: Context? = null
    fun initialize(context: Context?) {
        Base.context = context
    }

    fun getContext(): Context {
        synchronized(Base::class.java) {
            if (context == null) throw java.lang.NullPointerException("Call Base.initialize(context) within your Application onCreate() method.")
            return context!!.applicationContext
        }
    }

    val resources: Resources
        get() = getContext().resources
    val theme: Theme
        get() = getContext().theme
    val assets: AssetManager
        get() = getContext().assets
    val configuration: Configuration
        get() = resources.configuration
    val displayMetrics: DisplayMetrics
        get() = resources.displayMetrics
}
