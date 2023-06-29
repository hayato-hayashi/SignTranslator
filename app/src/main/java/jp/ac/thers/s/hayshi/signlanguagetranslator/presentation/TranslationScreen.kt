/*===========================================================
FABを押すとMediaPipeによる識別が開始される
識別中は識別された文を画面に表示する
もう一度FABを押すと識別が停止して、識別した文をChatGPTになげる
APIの通信が終わると返答を画面に出力する
============================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import jp.ac.thers.s.hayshi.signlanguagetranslator.R
import jp.ac.thers.s.hayshi.signlanguagetranslator.chat_gpt.ChatGPTViewModel
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.CustomLifecycle
import jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe.MediaPipeViewModel

@Composable
fun TranslationScreen (
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
                        contentDescription = "翻訳開始",
                        tint = Color.Red,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    ) {paddingValue ->
        Box(modifier = Modifier.padding(paddingValue)) {
            CameraPreview(
                customLifeCycle = customLifecycle,
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.TopCenter)
            )

            if (mediaPipeViewModel.flag) {
                Text(
                    text = result,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .offset(y = 50.dp)
                        .align(Alignment.CenterStart)
                )
            }
            else {
                Text(
                    text = content,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .offset(y = 50.dp)
                        .align(Alignment.CenterStart)
                )
            }
        }
    }
}