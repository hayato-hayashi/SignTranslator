package jp.ac.thers.s.hayshi.signlanguagetranslator.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    ) { paddingValue ->
        Column(modifier = Modifier.padding(paddingValue)) {
            for (item in chatGPTViewModel.getContents()) {
                Text(text = item)
            }
        }
    }
}
