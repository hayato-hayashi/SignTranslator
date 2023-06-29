/*
CameraXについて
https://developer.android.com/training/camerax?hl=ja

AndroidViewについて
https://note.com/masato1230/n/na09514fe5698
*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.presentation

import android.content.Context
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.MediaPipeViewModel
import java.util.concurrent.Executors
import androidx.compose.ui.Modifier
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.CustomLifecycle

@Composable
fun CameraPreview(
    customLifeCycle: CustomLifecycle,
    viewModel: MediaPipeViewModel = hiltViewModel(),
    modifier: Modifier,
) {

    // Composable関数内で従来のAndroidビューの作成と表示をする
    AndroidView(
        // Androidビューの作成をする
        factory = { context ->

            // PreviewViewのインスタンスを作成して、.applyで設定を行う
            val previewView = PreviewView(context).apply {

                // 表示領域と表示させるものの大きさが合わないときにどのようにするか(スケーリング方法)の設定
                this.scaleType = PreviewView.ScaleType.FILL_CENTER

                // 表示領域の設定
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // プレビューをするときの設定
            // .Builder()でプレビューの解像度やアスペクト比、プレビューの回転など設定することができる
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    // カメラのプレビューをpreviewView上に表示させる
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // 画像解析をするときの設定する
            //ImageAnalyzer : 画像をアプリに提供し、それらの画像に対して機械学習推論を実行する
            //setBackpressureStrategy : 前のフレームが処理中に新しいフレームが入力されたときの動作を設定する
            //STRATEGY_KEEP_ONLY_LATEST ->  キューをひとつに設定するので新しい画像で前の画像を上書きする
            //setTargetAspectRatio : 出力画像のアスペクト比を設定する
            //setOutputImageFormat : 出力画像の色形式を設定する
            //setTargetRotation : 出力画像を回転して正しい位置で表示する
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()


            // 画像解析をするスレッドを作成
            val executorService = Executors.newSingleThreadExecutor()

            // モデルのインスタンスを作成
            viewModel.getRecognizer(context)

            // カメラからの画像をモデルに入力して識別を行う
            imageAnalyzer.setAnalyzer(executorService) { image ->
                viewModel.recognizeLiveStream(image)
            }

            // CameraXの設定をする
            context.startCamera(
                lifecycleOwner = customLifeCycle,
                previewUseCase,
                imageAnalyzer,
            )

            return@AndroidView previewView
        },
        modifier = modifier
    )
}

// Context.とすることでContextオブジェクトを使用できるようになる
// システムリソースへのアクセスができるようになる
private fun Context.startCamera(
    lifecycleOwner: LifecycleOwner,
    vararg useCases: UseCase,
) {
    // ProcessCameraProviderはCameraXを操作するためのクラス。このクラスのインスタンスを利用してカメラの操作を行う
    // ProcessCameraProviderのインスタンスを生成する準備をする
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

    // cameraProvideFutureに値が入ってから処理が行われる
    cameraProviderFuture.addListener({
        try {
            // ProcessCameraProviderのインスタンスを取得する
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // CameraSelectorはカメラの設定をするオブジェクト
            // 前面カメラの使用はCameraSelector.LENS_FACING_FRONT
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // .bindToLifecycleは指定したライフサイクルに合わせてカメラとプレビューの開始と停止を自動的に処理する
            // カメラの使用方法(previewとImageAnalyzer)を設定している
            val camera: Camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, *useCases)
        } catch (e: Error) {
            // エラーが発生したときの処理
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(this))   // UIの更新やUI要素へのアクセスはメインスレッドで行う必要がある
}