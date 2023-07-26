/*
CameraXについて
https://developer.android.com/training/camerax?hl=ja
*/

package jp.ac.thers.s.hayshi.signlanguagetranslator

import android.content.Context
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.CustomLifecycle
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.MediaPipeViewModel
import java.util.concurrent.Executors

class Camera(
    val context: Context,
    val viewModel: MediaPipeViewModel,
//    val customLifecycle: CustomLifecycle,
) {
    lateinit var previewView: PreviewView
    lateinit var camera: Camera
    lateinit var imageAnalysis: ImageAnalysis

    init {
        setupCamera()
    }

    fun setupCamera() {
        // PreviewViewのインスタンスを作成して、.applyで設定を行う
        previewView = PreviewView(context).apply {
            // 表示領域と表示させるものの大きさが合わないときにどのようにするか(スケーリング方法)の設定
            this.scaleType = PreviewView.ScaleType.FILL_CENTER
            // 表示領域の設定
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // プレビューをするときの設定
        val previewUseCase = Preview.Builder().build().also {
            // カメラのプレビューをpreviewView上に表示させる
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // 画像解析をするときの設定する
        imageAnalysis = ImageAnalysis.Builder()
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
        imageAnalysis.setAnalyzer(executorService) { image ->
            viewModel.recognizeLiveStream(image)
        }

        // CameraXの設定をする
        context.startCamera(
//            lifecycleOwner = customLifecycle,
            previewUseCase,
            imageAnalysis,
        )
    }

    // Context.とすることでContextオブジェクトを使用できるようになる
    // システムリソースへのアクセスができるようになる
    private fun Context.startCamera(
//        lifecycleOwner: LifecycleOwner,
        vararg useCases: UseCase,
    ) {
        // ProcessCameraProviderはCameraXを操作するためのクラス。このクラスのインスタンスを利用してカメラの操作を行う
        // ProcessCameraProviderのインスタンスを生成する準備をする
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // cameraProviderFutureに値が入ってから処理が行われる
        cameraProviderFuture.addListener({
            try {
                // ProcessCameraProviderのインスタンスを取得する
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // CameraSelectorはカメラの設定をするオブジェクト
                // 背面カメラの使用はCameraSelector.LENS_FACING_BACK
                val cameraSelector: CameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                // .bindToLifecycleは指定したライフサイクルに合わせてカメラとプレビューの開始と停止を自動的に処理する
                // カメラの使用方法(previewとimageAnalysis)を設定している
                camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, *useCases)
            } catch (e: Exception) {
                // エラーが発生したときの処理
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))   // UIの更新やUI要素へのアクセスはメインスレッドで行う必要がある
    }

    // 画像解析を一時停止するメソッド
    fun pauseImageAnalysis() {
        imageAnalysis?.clearAnalyzer()
    }

    // プレビューを一時停止するメソッド
    fun pausePreview() {
        camera?.cameraControl?.enableTorch(false)
    }

    // 画像解析を再開するメソッド
    fun resumeImageAnalysis() {
        // モデルのインスタンスを作成
        viewModel.getRecognizer(context)

        // カメラからの画像をモデルに入力して識別を行う
        imageAnalysis?.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
            viewModel.recognizeLiveStream(image)
        }
    }

    // プレビューを再開するメソッド
    fun resumePreview() {
        camera?.cameraControl?.enableTorch(true)
    }
}