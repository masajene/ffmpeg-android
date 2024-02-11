package jp.cherpa_reserve.app.webview

import android.content.Context
import android.content.SharedPreferences

class SharedPref {
    val context: Context = Base.getContext()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PREFERENCE_NAME_APP", Context.MODE_PRIVATE)

    // 汎用 指定したキーが存在するかどうかチェック
    fun getKey(keyName: String): String {
        return sharedPreferences.getString(keyName, "") ?: ""
    }

    // 汎用 指定したキーが存在するかどうかチェック
    fun getFlag(keyName: String): Boolean {
        return sharedPreferences.getBoolean(keyName, false)
    }

    // 汎用 指定したキーに値を設定する
    fun setKey(keyName: String, key: String) {
        val editor = sharedPreferences.edit()
        editor.putString(keyName, key)
        editor.apply() // 保存
    }

    // 汎用 指定したキーに値を設定する
    fun setKey(keyName: String, key: Boolean) {

        val editor = sharedPreferences.edit()
        editor.putBoolean(keyName, key)
        editor.apply() // 保存
    }

    // 汎用 指定したキーを削除する
    fun removeKey(keyName: String) {
        val editor = sharedPreferences.edit()
        editor.remove(keyName)
            .apply()
    }

    // 汎用 値をクリアする
    fun clear() {
        val editor = sharedPreferences.edit()
        editor.clear().apply()
    }
}