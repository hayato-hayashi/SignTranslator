package jp.ac.thers.s.hayshi.signlanguagetranslator;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    // ListenableFutureは非同期操作の結果が格納される。また、その値に対する処理も登録することができる
    // ProcessCameraProviderはCameraXを操作するためのクラス。このクラスのインスタンスを利用してカメラの操作を行う
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreviewView previewView = findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // cameraProvideFutureに値が入ってから処理が行われる
        cameraProviderFuture.addListener(() -> {
                    try {
                        // ProcessCameraProviderオブジェクトを取得
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        bindPreview(cameraProvider, previewView);
                    } catch(ExecutionException | InterruptedException e) {

                    }
                },      // UIの更新やUI要素へのアクセスはメインスレッドで行う必要がある
                ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider, PreviewView previewView) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }
}
