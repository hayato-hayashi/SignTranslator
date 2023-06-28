package jp.ac.thers.s.hayshi.signlanguagetranslator.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val content: String?,
    val role: String?
)