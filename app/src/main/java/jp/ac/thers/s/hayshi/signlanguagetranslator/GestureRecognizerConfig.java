/*==================================================================================================
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

package jp.ac.thers.s.hayshi.signlanguagetranslator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult;

import java.util.List;

public class GestureRecognizerConfig {
    GestureRecognizer gestureRecognizer;
    Context context;
    TextView textView;

    Activity activity;

    GestureRecognizerConfig(Context context, TextView textView, Activity activity,
            Float minHandDetectionConfidence, Float minHandTrackingConfidence,
            Float minHandPresenceConfidence, RunningMode runningMode
    ) {
        this.textView = textView;
        this.activity = activity;

        // モデルの大まかな設定
        BaseOptions.Builder baseOptionBuilder = BaseOptions.builder();

        // モデルの動作をCPUで実行する
        baseOptionBuilder.setDelegate(Delegate.CPU);

        // モデルが存在するパスを記述
        baseOptionBuilder.setModelAssetPath("gesture_recognizer.task");


        try {
            BaseOptions baseOptions = baseOptionBuilder.build();

            // モデルの細かい設定
            GestureRecognizer.GestureRecognizerOptions.Builder optionsBuilder =
                    GestureRecognizer.GestureRecognizerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinHandDetectionConfidence(minHandDetectionConfidence)
                    .setMinTrackingConfidence(minHandTrackingConfidence)
                    .setMinHandPresenceConfidence(minHandPresenceConfidence)
                    .setRunningMode(runningMode);

            // LIVE_STREAMの場合は追加で設定が必要
            if(runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                        .setResultListener(this::returnLiveStreamResult)
                        .setErrorListener(this::returnLivestreamError);
            }
            GestureRecognizer.GestureRecognizerOptions options = optionsBuilder.build();

            // モデルの作成
            this.gestureRecognizer = GestureRecognizer.createFromOptions(context, options);
        } catch(Error e) {
            // エラー処理
            e.printStackTrace();
        }
    }

    public void recognizeLiveStream (
        // ImageProxyはCameraXライブラリで使用されるクラス
        // カメラから得られたフレーム画像へのアクセスができる
        ImageProxy imageProxy
    ) {
        // 現在の時間をミリ秒単位で取得する
        long frameTime = SystemClock.uptimeMillis();

        // imageProxyと同じサイズの空のビットマップを作成
        Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888
        );

        // imageProxyに格納されているフレーム画像データをbitmapBufferにコピー
        imageProxy.getPlanes()[0].getBuffer().rewind();
        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

        // matrixオブジェクトは2Dグラフィックスの変換操作をすることができる
        Matrix matrix = new Matrix();

        // postRotate : 回転変換
        // imageProxy.getImageInfo().getRotationDegrees()はフレームの回転情報を取得
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

        // postScale : スケール変換(画像のサイズを変更する)
        // x軸方向に画像を反転させている
        matrix.postScale(
                -1f, 1f, imageProxy.getWidth(), imageProxy.getHeight()
        );

        // bitmapBufferにmatrixの操作を適用して、画像を正しい向きにしている
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.getWidth(),
                bitmapBuffer.getHeight(),
                matrix,
                true
        );

        // BitmapをMPImage(モデルの入力に適したオブジェクト)に変換する
        MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();

        // 画像とタイムスタンプ(時間情報)を提供して識別を開始
        // タイムスタンプを提供することでフレームの前後関係を正確に確立する
        // 非同期で実行されて、バックグラウンドスレッドで実行される
        // 前のフレームの処理が追いついていないときは新しい入力を受け付けない
        if(this.gestureRecognizer != null) {
            gestureRecognizer.recognizeAsync(mpImage, frameTime);
        }
    }

    // 識別が終わったあとに実行される
    private void returnLiveStreamResult (
        GestureRecognizerResult result,
        MPImage input
    ) {
        List<List<Category>> gestures = result.gestures();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (List<Category> gestureCategories : gestures) {
                    for (Category category : gestureCategories) {
                        // カテゴリ名の取得
                        String categoryName = category.categoryName();
                        // カテゴリ名の利用
                        textView.setText(categoryName);
                        System.out.println(categoryName);
                    }
                }
            }
        });
    }

    // 識別中にエラーが発生すると実行される
    private void returnLivestreamError(RuntimeException error) {
        textView.setText(error.getMessage());
    }
}
