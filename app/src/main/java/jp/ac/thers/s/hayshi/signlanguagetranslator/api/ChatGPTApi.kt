/*=================================================================================
Retrofit            : HTTP通信の設定をアノテーション(@)を使用して記述する
                      OkHttpの機能を活用しながら、API通信のコードをシンプルで簡潔に記述する

@Headers            : 通信のHeaderの内容を記述する
@POST               : 引数のURIに対してPOSTをリクエストする
@Body               : サーバーに送信する情報を記述する

ChatGPTDto          : サーバーから返されるデータの型
ChatGPTRequestData  : サーバーに送信するデータの型

suspend             : 非同期で処理を実行する

Retrofit
https://square.github.io/retrofit/
OpenAI APIReference
https://platform.openai.com/docs/api-reference/chat
================================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.api

import jp.ac.thers.s.hayshi.signlanguagetranslator.common.Constants
import jp.ac.thers.s.hayshi.signlanguagetranslator.response.ChatGPTDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGPTApi {

    @Headers("Authorization: Bearer ${Constants.API_KEY}")
    @POST("chat/completions")
    suspend fun chatWithChatGPT(@Body requestData: ChatGPTRequestData): ChatGPTDto
}