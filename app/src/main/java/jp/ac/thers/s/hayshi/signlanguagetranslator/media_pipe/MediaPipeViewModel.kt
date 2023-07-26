package jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.State
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

@HiltViewModel
class MediaPipeViewModel @Inject constructor() : ViewModel() {

    // 翻訳を実行しているかどうかの状態を管理する
    // trueのときに翻訳を実行中
    var flag by mutableStateOf(false)

    // 識別結果を格納する
    var result: String = ""

    // 画面に表示する文字列を格納する
    var _result by mutableStateOf<List<String>>(emptyList())

    // 同じ手を何回出したかを記録するための変数
    var count: Int = 0

    // ジェスチャー検出をするモデル
    private var gestureRecognizer: MediaPipe? = null

    fun getRecognizer(context: Context) {
        gestureRecognizer = MediaPipe(
            context,
            ::returnLiveStreamResult,
            ::returnLiveStreamError
        )
    }

    // 識別が終了したときに実行する処理
    private fun returnLiveStreamResult(result: GestureRecognizerResult, input: MPImage) {
        val gestures = result.gestures()
        for (gestureCategories in gestures) {
            for (category in gestureCategories) {
                // カテゴリ名の取得
                val categoryName = category.categoryName()

                // 同じ手を7回識別すると画面に表示される
                if (this.result != "") {
                    if (this.result == categoryName) {
                        count++
                        System.out.println("count: ${count}, result: ${this.result}")
                    } else if (categoryName == "none")
                    else {
                        count = 0
                        this.result = ""
                    }
                } else {
                    if (categoryName != "none") this.result = categoryName
                }

                if (count >= 7) {
                    _result = _result.plus(this.result)
                    count = 0
                    this.result = ""
                }
            }
        }
    }

    // 識別中にエラーが発生すると実行される
    private fun returnLiveStreamError(error: RuntimeException) {
        error.printStackTrace()
    }

    // 識別を実行する
    fun recognizeLiveStream(imageProxy: ImageProxy) {
        gestureRecognizer?.recognizeLiveStream(imageProxy)
    }

    // ボタンを押したときに状態を反転させる
    fun changeFlag() {
        flag = !flag
    }

    // 状態を初期化する
    fun clear() {
        result = ""
        _result = emptyList()
        count = 0
    }
}
