package jp.ac.thers.s.hayshi.signlanguagetranslator.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Choice(
    @Json(name = "finish_reason")
    val finishReason: String?,
    val index: Int?,
    val message: Message?
)