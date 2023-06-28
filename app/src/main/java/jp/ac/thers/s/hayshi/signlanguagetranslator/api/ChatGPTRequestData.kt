package jp.ac.thers.s.hayshi.signlanguagetranslator.api

import jp.ac.thers.s.hayshi.signlanguagetranslator.response.Message

data class ChatGPTRequestData(
    val model: String,
    val messages: List<Message>,
)
