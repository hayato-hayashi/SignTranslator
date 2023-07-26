/*=================================================================================================
インターフェースの場合や外部ライブラリのクラスなど、コンストラクタにインスタンスを代入できないときがある。
このような場合は、Hiltモジュールを使用して依存関係を記述する

@Module                 : このアノテーションが付けらたクラスがHiltモジュールになる
@InstallIn              : モジュールで定義した依存関係がどのコンポーネントで使用されるかを引数で指定する
                          また、依存関係の注入のタイミングは指定したコンポーネントによって変わる
@Provides               : 注入されるインスタンスを返す関数を定義する

https://developer.android.com/training/dependency-injection/hilt-android?hl=ja#hilt-modules
==================================================================================================*/

/*=========================================================================================
Retrofit.Builder()               : Retrofitの設定を開始する
.baseUrl()                       : APIのベースURLを設定する
.client()                        : API通信の細かい設定をする
.addConverterFactory()           : 通信の結果のJsonデータをどのように変換するか設定する
MoshiConverterFactory.create()   : 入力データがJsonであると設定
.add(KotlinJsonAdapterFactory()) : 出力データがKotlinであると設定
.create()                        : javaのクラスオブジェクトのインスタンスを生成する
UnsplashApi::class.java          : ChatGPTApiクラスをjavaのクラスオブジェクトに変換する
=========================================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.ac.thers.s.hayshi.signlanguagetranslator.api.ChatGPTApi
import jp.ac.thers.s.hayshi.signlanguagetranslator.common.Constants.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChatGPTApi(): ChatGPTApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // 接続タイムアウト時間を設定 (30秒)
                    .readTimeout(30, TimeUnit.SECONDS) // 読み取りタイムアウト時間を設定 (30秒)
                    .writeTimeout(30, TimeUnit.SECONDS) // 書き込みタイムアウト時間を設定 (30秒)
                    .build()
            )
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                )
            )
            .build()
            .create(ChatGPTApi::class.java)
    }
}