/*============================================================
ViewModelは画面で発生する処理やデータの管理をする
また、アプリが終了されるまでメモリ内に残るので、変数の再宣言が発生しない

.onEach
flow型のデータを処理するのに使用される
resultにはemit()で送信されてくるデータが入る
今回は1度のデータの取得で一度しかemit()をしないので使わなくても動作する

.launchIn(viewModelScope)
viewModelScope内でflowを実行して、非同期の処理を実行する
===========================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.chat_gpt

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.thers.s.hayshi.signlanguagetranslator.api.ChatGPTRequestData
import jp.ac.thers.s.hayshi.signlanguagetranslator.common.NetworkResponse
import jp.ac.thers.s.hayshi.signlanguagetranslator.response.Message
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatGPTViewModel @Inject constructor(
    private val chatGPTUseCase: ChatGptUseCase
) : ViewModel() {

    // chatGPTからの返答を格納して、この値を画面に表示する
    var content by mutableStateOf<List<String>>(emptyList())

    // chatGPTのモデルを指定
    var model = "gpt-3.5-turbo"

    // chatGPTが返答をする際の設定を記述
    var message = Message(role = "system", content = "正しい日本語に変換してください")

    // chatGPTに聞きたい内容をcontentに記述する
    var message2: Message = Message(role = "user", content = "")

    fun chat(message: String) {
        // POSTするときのデータの形に変換する
        message2 = Message(role = "user", content = message)
        val chatGPTRequestData = ChatGPTRequestData(model, listOf(this.message, this.message2))

        // 取得したデータの処理を記述
        chatGPTUseCase(chatGPTRequestData).onEach { result ->
            content = result
        }.launchIn(viewModelScope)
    }

    // 値の初期化
    fun clear() {
        content = emptyList()
    }
}
