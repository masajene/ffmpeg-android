package jp.cherpa_reserve.app.webview

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import jp.cherpa_reserve.app.webview.databinding.ActivitySettingBinding

class SettingActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = SharedPref().getKey("url")
        binding.textView.setText(url)
        binding.button.setOnClickListener {
            SharedPref().setKey("url", binding.textView.text.toString())
            SharedPref().setKey("noOperationTimeoutTime", binding.timeoutEditText.text.toString().toLong() * 1000)
            finish()
        }

        var noOperationTimeoutTime = SharedPref().getLong("noOperationTimeoutTime")
        if (noOperationTimeoutTime == 0L) {
            noOperationTimeoutTime = 1000 * 60
        }
        binding.timeoutEditText.setText((noOperationTimeoutTime / 1000).toString())
    }
}