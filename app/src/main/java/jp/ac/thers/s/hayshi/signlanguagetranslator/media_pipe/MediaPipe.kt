/*==================================================================================================
MediaPipe Solutions
https://developers.google.com/mediapipe/solutions/vision/gesture_recognizer

Sample
https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android/app/src/main/java/com/google/mediapipe/examples/gesturerecognizer
モデルの設定
setMinHandDetectionConfidence
検出の信頼度スコアの最低値を設定して、これより高い信頼度の場合検出成功

setMinTrackingConfidence
トラッキングでは前のフレームと今のフレームでの手の位置の一致度がある程度高くないといけない
一致度のしきい値を設定する

setMinHandPresenceConfidence
手が存在しているかどうかの信頼度の基準値を設定
信頼度が基準値より低いと手が存在しないと判断されてパーム検出モデルが起動する
信頼度が基準値より高いと手が存在すると判断されてトラッキング検出などが起動する

setResultListener
識別結果を受け取ったあとの処理を記述する。処理は非同期で実行される
Live_Streamのときのみ設定が必要

setErrorListener
エラーが発生したときの処理を記述する

setRunningMode
モデルへの入力データを設定する
Image: 画像入力
Video: 動画入力
Live_Stream: リアルタイムで生成される映像を入力
==================================================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult

class MediaPipe(
    val context: Context,
    val returnLiveStreamResult: (GestureRecognizerResult, MPImage) -> Unit,
    val returnLivestreamError: (RuntimeException) -> Unit,
) {
    // モデルの初期値をnullに設定
    private var gestureRecognizer: GestureRecognizer? = null

    // インスタンス生成時に自動で実行される
    init {
        // モデルの設定
        setupGestureRecognizer()
    }

    fun setupGestureRecognizer() {
        // モデルの大まかな設定
        val baseOptionBuilder = BaseOptions.builder()

        // モデルの動作をCPUで実行する
        baseOptionBuilder.setDelegate(Delegate.CPU)

        // モデルが存在するパスを記述
        baseOptionBuilder.setModelAssetPath("gesture_recognizer.task")

        try {
            val baseOptions = baseOptionBuilder.build();

            // モデルの細かい設定
            val optionsBuilder =
                GestureRecognizer.GestureRecognizerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinHandDetectionConfidence(0.5f)
                    .setMinTrackingConfidence(0.5f)
                    .setMinHandPresenceConfidence(0.5f)
                    .setRunningMode(RunningMode.LIVE_STREAM)

            // LIVE_STREAMの場合は追加で設定が必要
            optionsBuilder
                .setResultListener(returnLiveStreamResult)
                .setErrorListener(returnLivestreamError)
            val options = optionsBuilder.build()

            // モデルの作成
            gestureRecognizer = GestureRecognizer.createFromOptions(context, options)
        } catch (e: Error) {
            e.printStackTrace()
        }
    }

    fun recognizeLiveStream (
        // ImageProxyはCameraXライブラリで使用されるクラス
        // カメラから得られたフレーム画像へのアクセスができる
        imageProxy: ImageProxy,
    ) {
        // 現在の時間をミリ秒単位で取得する
        val frameTime = SystemClock.uptimeMillis()

        // imageProxyと同じサイズの空のビットマップを作成
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )

        // imageProxyに格納されているフレーム画像データをbitmapBufferにコピーした
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        // matrixオブジェクトは2Dグラフィックスの変換操作をすることができる
        // applyのブロックの中に操作内容を記述する
        val matrix = Matrix().apply {
            // postRotate : 回転変換
            // imageProxy.imageInfo.rotationDegrees.toFloat()はフレームの回転情報を取得している
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // postScale : スケール変換(画像のサイズを変更する)
            // x軸方向に画像を反転させている
            postScale(
                -1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat()
            )
        }

        // bitmapBufferにmatrixの操作を適用して、画像を正しい向きにしている
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer,
            0,
            0,
            bitmapBuffer.width,
            bitmapBuffer.height,
            matrix,
            true
        )

        // BitmapをMPImage(モデルの入力に適したオブジェクト)に変換する
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        // 画像とタイムスタンプ(時間情報)を提供して識別を開始
        // タイムスタンプを提供することでフレームの前後関係を正確に確立する
        // 非同期で実行されて、バックグラウンドスレッドで実行される
        // 前のフレームの処理が追いついていないときは新しい入力を受け付けない
        if (gestureRecognizer != null) {
            gestureRecognizer!!.recognizeAsync(mpImage, frameTime)
        }
    }

}