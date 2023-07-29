package jp.ac.thers.s.hayshi.signlanguagetranslator.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import jp.ac.thers.s.hayshi.signlanguagetranslator.ScreenRoute
import jp.ac.thers.s.hayshi.signlanguagetranslator.chat_gpt.ChatGPTViewModel

@Composable
fun LogScreen(
    navController: NavController,
    chatGPTViewModel: ChatGPTViewModel,
) {
    Scaffold (
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(ScreenRoute.TranslationScreen.route)
                },
                backgroundColor = Color.Black,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "翻訳画面に遷移",
                    tint = Color.Blue,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    ) { it ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(140, 171, 216))
                .padding(it)
        ) {
            LazyColumn()
            {
                itemsIndexed(chatGPTViewModel.getContents()) { index, it ->
                    Box(
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 10.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier
                                .sizeIn(minWidth = 0.dp, minHeight = 0.dp)  // 要素の最小サイズを定義する。0にすることで要素のサイズを可変にすることができる
                                .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(color = Color(121,226,120))
                                .padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
