/*==================================================================================================
NavHostを使用して画面遷移をJetpack Composeで実現する
NavHostにはNavGraphとNavControllerの2つの要素が必要
NavGraph        : 遷移したあとの画面を記述する(Webページのログイン画面、メニュー画面みたいな感じ)
NavController   : 表示する画面を切り替えるAPI等を記述
遷移先の画面の指定はurlみたいなもので行う(Webページと同じ感じ)

rememberNavController()
NavControllerのインスタンスを生成
そのインスタンスをメモリに格納することで再レンダリングされてもインスタンスを保持できる

navHost(
    navController = ,       使用するNavControllerを記述
    startDestination = ,    最初に表示する画面のurlを記述
) {
    composable(route = この画面のurlを設定) {
        表示する内容を記述
    }
    composable(route = この画面のurlを設定 + "/{photoId}") {    //queryを受け取ることもできる
        表示する内容を記述
    }
    ...
}

詳しい内容は下のURL
https://developer.android.com/jetpack/compose/navigation?hl=ja
==================================================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.thers.s.hayshi.signlanguagetranslator.chat_gpt.ChatGPTViewModel
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.CustomLifecycle
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.MediaPipeViewModel
import jp.ac.thers.s.hayshi.signlanguagetranslator.presentation.LogScreen
import jp.ac.thers.s.hayshi.signlanguagetranslator.presentation.TranslationScreen
import jp.ac.thers.s.hayshi.signlanguagetranslator.ui.theme.SignLanguageTranslatorTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sharedViewModel: ChatGPTViewModel by viewModels()
    private val mediaPipeViewModel: MediaPipeViewModel by viewModels()
    private val customLifecycle = CustomLifecycle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val camera: Camera = Camera(this, mediaPipeViewModel)

        setContent {
            SignLanguageTranslatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = ScreenRoute.TranslationScreen.route,
                    ) {
                        // 翻訳画面
                        composable(route = ScreenRoute.TranslationScreen.route) {
                            TranslationScreen(navController, sharedViewModel, camera, mediaPipeViewModel)
                        }

                        // 過去の翻訳結果表示画面
                        composable(route = ScreenRoute.LogScreen.route) {
                            LogScreen(navController, sharedViewModel)
                        }
                    }
                }
            }
        }
    }
}