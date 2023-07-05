/*===========================================================
FABを押すとMediaPipeによる識別が開始される
識別中は識別された文を画面に表示する
もう一度FABを押すと識別が停止して、識別した文をChatGPTになげる
APIの通信が終わると返答を画面に出力する
============================================================*/

/*=============================================================
marginとpaddingの指定方法
Kotlinにはmarginを指定するmodifierがないのでpadding()で代用する

コンポーネントの大きさを指定する前のpadding -> marginとして機能
コンポーネントの大きさを指定した後のpadding -> paddingとして機能
上のような認識でmarginとpaddingを指定する
=============================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import jp.ac.thers.s.hayshi.signlanguagetranslator.R
import jp.ac.thers.s.hayshi.signlanguagetranslator.chat_gpt.ChatGPTViewModel
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.CustomLifecycle
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.MediaPipeViewModel

@Composable
fun TranslationScreen (
    navController: NavController,
    chatGPTViewModel: ChatGPTViewModel = hiltViewModel(),
    mediaPipeViewModel: MediaPipeViewModel = hiltViewModel(),
) {
    // カメラの一時停止と再開を制御するためのもの
    val customLifecycle = CustomLifecycle()

    // 識別結果を格納
    val result = mediaPipeViewModel._result.joinToString("")

    // ChatGPTからの返答が格納
    val content = chatGPTViewModel.content.joinToString("")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    mediaPipeViewModel.changeFlag()
                    if (mediaPipeViewModel.flag) {
                        mediaPipeViewModel.clear()
                        chatGPTViewModel.clear()
                        customLifecycle.doStart()
                    }
                    else {
                        customLifecycle.doPause()
                        if (result !== "") chatGPTViewModel.chat(result)
                    }
                },
                backgroundColor = Color.Black,
            ) {
                if (!mediaPipeViewModel.flag) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "翻訳開始",
                        tint = Color.Green,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.round_stop_24),
                        contentDescription = "翻訳終了",
                        tint = Color.Red,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    ) { paddingValue ->
        Box() {
            Column(
                modifier = Modifier.padding(paddingValue),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                CameraPreview(
                    customLifeCycle = customLifecycle,
                    modifier = Modifier
                        .fillMaxWidth()                         // 幅を画面全体に設定
                        .padding(start = 10.dp, end = 10.dp)    // 画面の幅から左右10.dpずつ間隔を開ける
                        .weight(2f)                             // 高さを設定
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = if(mediaPipeViewModel.flag) result else content,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)    // 外部の余白を設定
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = Color.Gray)
                        .padding(16.dp)                         // 内部の余白を設定
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
            if (!mediaPipeViewModel.flag) {
                Button(
                    modifier = Modifier.size(width = 120.dp, height=50.dp).align(BottomCenter).offset(y = (-10).dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                    onClick = { /*TODO*/ }
                ) {
                    // ボタンの中身のUIを記述する。横方向に要素が並ぶ(Rowと同じ)
                    Text(text = "Log", color = Color.White)
                }
            }
        }
    }
}