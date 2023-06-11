package jp.ac.thers.s.hayshi.signlanguagetranslator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // ListenableFutureは非同期操作の結果が格納される。また、その値に対する処理も登録することができる
    // ProcessCameraProviderはCameraXを操作するためのクラス。このクラスのインスタンスを利用してカメラの操作を行う
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;

    // スレッドを事前に準備する(スレッドプールを使用する) => リソースの効率的な活用などのメリットがある
    // スレッドプールには1つのスレッドが入っている
    private Executor cameraExecutor = Executors.newSingleThreadExecutor();

    // 画像を表示するコンポーネント
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreviewView previewView = findViewById(R.id.previewView);
        Button takePicture = findViewById(R.id.takePicture);
        takePicture.setOnClickListener(this);
        imageView = findViewById(R.id.imageView);

        // ProcessCameraProviderのインスタンスを生成する準備をする
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // cameraProvideFutureに値が入ってから処理が行われる
        cameraProviderFuture.addListener(() -> {
                    try {
                        // 実際にProcessCameraProviderのインスタンスを取得する
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                        // プレビューをするときの設定
                        // .Builder()でプレビューの解像度やアスペクト比、プレビューの回転など設定することができる
                        Preview preview = new Preview.Builder().build();

                        // 画像キャプチャをするときの設定
                        // 処理(レイテンシ)を早くするモードと画質をよくするモードの2つある
                        // 写真の回転情報を設定する
                        imageCapture = new ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .setTargetRotation(previewView.getDisplay().getRotation())
                                .build();

                        // CameraSelectorはカメラの設定をするオブジェクト
                        // 前面カメラの使用はCameraSelector.LENS_FACING_FRONT
                        CameraSelector cameraSelector = new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build();

                        //.bindToLifeCycleは指定したライフサイクルに合わせてカメラとプレビューの開始と停止を自動的に処理する
                        // カメラの使用方法を設定している
                        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

                        // previewの出力先をpreviewViewに設定している
                        preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    } catch (ExecutionException | InterruptedException e) {

                    }
                },      // UIの更新やUI要素へのアクセスはメインスレッドで行う必要がある
                ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.takePicture) {
            // 撮影した写真の保存先ファイルを指定する
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(createPhotoFile()).build();

            // 写真の撮影を開始
            imageCapture.takePicture(outputFileOptions, cameraExecutor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            // 写真の保存が完了したときに呼び出される
                            // 保存されたファイルのパスを取得
                            String savedFilePath = outputFileResults.getSavedUri().getPath();

                            // 保存された画像ファイルを読み込む
                            Bitmap bitmap = BitmapFactory.decodeFile(savedFilePath);

                            // 回転を補正するためのMatrixを作成
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90); // 回転角度を指定

                            // ビットマップを回転させる
                            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                            // ImageViewにビットマップを設定する
                            // 画像の表示はUIの変更に関わるのでメインスレッド上で実行する
                            runOnUiThread(() -> {
                                imageView.setImageBitmap(rotatedBitmap);
                            });
                        }
                        @Override
                        public void onError(ImageCaptureException error) {
                            // 写真の保存中にエラーが発生したときに呼び出される
                            int errorCode = error.getImageCaptureError();
                            String errorMessage;
                            switch (errorCode) {
                                case ImageCapture.ERROR_CAPTURE_FAILED:
                                    errorMessage = "写真のキャプチャが失敗しました。";
                                    break;
                                case ImageCapture.ERROR_FILE_IO:
                                    errorMessage = "ファイルの入出力エラーが発生しました。";
                                    break;
                                case ImageCapture.ERROR_INVALID_CAMERA:
                                    errorMessage = "無効なキャプチャモードが指定されました。";
                                    break;
                                default:
                                    errorMessage = "エラーが発生しました。";
                                    break;
                            }
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    private File createPhotoFile() {
        // 現在のアプリケーションのコンテキストを取得
        // コンテキスト => アプリケーションの実行環境やリソースにアクセスするための情報を提供するオブジェクト
        Context context = getApplicationContext();
        // アプリ固有のストレージにファイルを保存する
        File file = new File(context.getFilesDir(), "hayato.jpg");
        return file;
    }
}


