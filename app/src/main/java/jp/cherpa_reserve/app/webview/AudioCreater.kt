package jp.cherpa_reserve.app.webview

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

class AudioCreater(private  val context: Context) {

    fun combineAndPlayAudio(number: String) {
        val outputFilePath = context.filesDir.absolutePath + "/output.mp3"

        // リソースからファイルにコピー
        val list = generateAudioFileList(number)
        val cmd = mutableListOf<String>()
        list.forEachIndexed { index, fileName ->
            copyRawResourceToAppDirectory(context, getResourceId(fileName, context), "voice_${index + 1}.mp3")
            cmd.add("-i")
            cmd.add(context.filesDir.absolutePath + "/voice_${index + 1}.mp3")
        }
        cmd.add( "-filter_complex")
        cmd.add("concat=n=${list.size}:v=0:a=1[outa]")
        cmd.add("-map")
        cmd.add("[outa]")
        cmd.add(outputFilePath)

        val outputFile = File(outputFilePath)
        if (outputFile.exists()) {
            outputFile.delete()
        }

        // FFmpegを非同期で実行します。
        FFmpeg.executeAsync(cmd.toTypedArray()) { _, returnCode ->
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                // 音声生成が成功したら再生を開始
                println("生成成功")
                playCombinedAudio(outputFilePath)
            } else {
                println("音声結合中にエラーが発生しました。")
            }
        }
    }

    private fun generateAudioFileList(number: String): List<String> {
        val length = number.length
        return number.mapIndexed { index, digit ->
            val zeros = "0".repeat(length - index - 1)
            if (digit != '0') "voice_$digit$zeros" else ""
        }.filter { it.isNotEmpty() }
    }

    private fun getResourceId(resourceName: String, context: Context): Int {
        return context.resources.getIdentifier(resourceName, "raw", context.packageName)
    }

    private fun playCombinedAudio(outputFilePath: String) {
        // 生成された音声ファイルを再生
        MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(outputFilePath)
            prepare()
            start()
        }
    }

    private fun copyRawResourceToAppDirectory(context: Context, resId: Int, fileName: String) {
        val inputStream = context.resources.openRawResource(resId)
        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}