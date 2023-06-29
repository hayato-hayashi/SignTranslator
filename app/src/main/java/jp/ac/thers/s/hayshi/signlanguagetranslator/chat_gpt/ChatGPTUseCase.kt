/*==================================================================================================
invoke
クラスのメソッドではあるが、メソッド名を指定することなく処理を実行することができる
chatGPTUseCase: ChatGPTUseCase = ChatGPTUseCase()
のようにインスタンスを作成したあとにchatGPTUseCase()とすることでinvoke()で記述した処理を実行する
引数としてrequestDataを受け取り、戻り値としてFlow型の情報を返す
invokeメソッドであることを示すためにoperator fun をつける必要がある

Flow
データを非同期で取得して、そのデータを送信するという2つのことを記述することができる
flow { この中に2つの内容を記述する }
api.chatWithChatGPT(requestData)でデータを取得して(.toContentでデータの形を変換)
emit()でデータを送信する

invoke()を実行するとflowで定義した処理が実行されて、emit()で送信されたデータが戻り値となる

Flowについて
https://developer.android.com/kotlin/flow?hl=ja
==================================================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.chat_gpt

import jp.ac.thers.s.hayshi.signlanguagetranslator.api.ChatGPTApi
import jp.ac.thers.s.hayshi.signlanguagetranslator.api.ChatGPTRequestData
import jp.ac.thers.s.hayshi.signlanguagetranslator.common.NetworkResponse
import jp.ac.thers.s.hayshi.signlanguagetranslator.response.toContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatGptUseCase @Inject constructor(
    private val api: ChatGPTApi
){

    operator fun invoke(requestData: ChatGPTRequestData): Flow<List<String>> = flow {
        try {
            // apiをたたいてデータを取得する
            val chat = api.chatWithChatGPT(requestData).toContent()

            // データを送信
            emit(chat)
        } catch (e: Exception) {
            System.out.println(e.message.toString())
        }
    }
}