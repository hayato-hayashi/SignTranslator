/*====================================================================
オブジェクト変換エクステンション(DTO)
APIをたたいて取得されるデータ(Json)をアプリで使用するデータ構造に変換したもの

ChatGPTDto.関数名
にするとChatGPTDtoが持っているプロパティに直接アクセスできる

!! : オブジェクトを非nullとして扱うために使用される
=====================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatGPTDto(
    val choices: List<Choice?>?,
    val created: Int?,
    val id: String?,
    val model: String?,
    @Json(name = "object")
    val objectX: String?,
    val usage: Usage?
)

// 取得したデータからChatGPTの返答だけを取り出す
fun ChatGPTDto.toContent(): List<String> {
    return choices!!.map {it ->
        it?.message?.content.toString()
    }
}