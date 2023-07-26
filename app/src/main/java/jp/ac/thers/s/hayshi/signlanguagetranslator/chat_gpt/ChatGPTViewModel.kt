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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.thers.s.hayshi.signlanguagetranslator.api.ChatGPTRequestData
import jp.ac.thers.s.hayshi.signlanguagetranslator.response.Message
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatGPTViewModel @Inject constructor(
    private val chatGPTUseCase: ChatGptUseCase
) : ViewModel() {

    // chatGPTからの返答を格納して、この値をTranslationScreenに表示する
    private var content by mutableStateOf<List<String>>(emptyList())

    // LogScreen画面に表示する内容を格納
    private var log by mutableStateOf<MutableList<String>>(mutableListOf())

    // chatGPTのモデルを指定
    private var model = "gpt-3.5-turbo"

    // chatGPTが返答をする際の設定を記述
    private var message = Message(role = "system", content = "文章は以下のような特徴を持っている可能性があります\n" +
            "・「の」、「も」、「り」、「を」、「ん」がない文章\n" +
            "・「お」が「ろ」と間違って変換されている\n" +
            "・伸ばし棒がなく、「こーす」が「こうす」のように表現されている\n" +
            "・濁音、半濁音がなく、かわりに「が」を「か」のように表現されている\n" +
            "・拗音がなく、かわりに「しょ」が「しよ」のように表現されている\n" +
            "以下の文章に文字を補完して、正しい言葉に変換してください\n" +
            "変換が必要ない文章である場合は、そのまま表示してください\n" +
            "\n" +
            "例 きふたいかく　ぎふだいがく\n")

    // chatGPTに聞きたい内容をcontentに記述する
    private var message2: Message = Message(role = "user", content = "")

    // chatGPTからの返答の状態を表す
    var isLoading by mutableStateOf(false)

    fun chat(message: String) {
        isLoading = true

        // POSTするときのデータの形に変換する
        message2 = Message(role = "user", content = message)
        val chatGPTRequestData = ChatGPTRequestData(model, listOf(this.message, this.message2))

        // 取得したデータの処理を記述
        chatGPTUseCase(chatGPTRequestData).onEach { result ->
            content = result
        }.launchIn(viewModelScope)
    }

    // ChatGPTからの返答を1つの文字列に変換して渡す
    fun getContent(): String {
        isLoading = false
        return content.joinToString("")
    }

    // 値の初期化
    fun clear() {
        content = emptyList()
    }

    // 翻訳結果を格納する
    fun setContent(content: String) {
        log.add(content)
    }

    // 今までの翻訳結果を渡す
    fun getContents(): MutableList<String> {
        return log
    }
}
