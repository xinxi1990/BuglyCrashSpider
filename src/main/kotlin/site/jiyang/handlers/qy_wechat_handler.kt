package site.jiyang.handlers

import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import site.jiyang.model.BuGlyAuthFailedResp
import site.jiyang.model.Issue
import site.jiyang.model.config.Config
import site.jiyang.model.config.QyWeChatBot
import java.util.concurrent.TimeUnit

/**
 * 针对企业微信 IHandler
 */
class QYWeChatHandler(qyWeChatBotConfig: QyWeChatBot, private val config: Config) : IHandler {
    private val botSender = QYWeChatBotSender(qyWeChatBotConfig)

    override fun handleIssuesResp(issues: List<Issue>) {
        issues.forEach {
            val markdown = QYWeChatBotMarkdown(
                h1 = "New issue",
                h2 = "#${it.issueId} ${it.exceptionName}",
                desc = it.lastestUploadTime,
                code = it.keyStack,
                post = it.exceptionMessage,
                li = arrayOf(buildIssueLink(it.issueId))
            )
            botSender.send(markdown)
        }
    }

    override fun handleAuthFailedResp(authFailedResp: BuGlyAuthFailedResp) {
        botSender.send(QYWeChatBotMarkdown(h1 = "Auth failed", desc = authFailedResp.toString()))
    }

    override fun handleUnknownResp(unknownResp: String) {
        botSender.send(QYWeChatBotMarkdown(h1 = "Unknown resp", post = unknownResp))
    }

    override fun handleException(e: Exception) {
        botSender.send(QYWeChatBotMarkdown(h2 = "Spider Exception", post = "$e"))
    }

    private fun buildIssueLink(issueId: String) =
        "${config.buGlyHost}/crash-reporting/crashes/${config.query.appId}/$issueId?pid=${config.query.pid}"
}

//region Bot Sender

class QYWeChatBotSender(private val qyWeChatBotConfig: QyWeChatBot) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json".toMediaTypeOrNull()

    fun send(QYWeChatBotMarkdown: QYWeChatBotMarkdown) {
        val msg = QYWeChatBotMsg(QYWeChatMarkdown(QYWeChatBotMarkdown.toStrContent()))
        val json = Moshi.Builder().build().adapter(QYWeChatBotMsg::class.java).toJson(msg)
        val byteArray = json.toByteArray()
        val body = byteArray.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(qyWeChatBotConfig.webHook)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()
        val response = client.newCall(request).execute()
        println(response.code)
        println(response.message)
        println(response.body?.string())
    }
}
//endregion

//region Model Class
data class QYWeChatBotMsg(val QYWeChatMarkdown: QYWeChatMarkdown, val msgtype: String = "markdown")

data class QYWeChatMarkdown(val content: String)

class QYWeChatBotMarkdown(
    private val h1: String? = null,
    private val h2: String? = null,
    private val desc: String? = null,
    private val code: String? = null,
    private val post: String? = null,
    private val li: Array<String> = emptyArray()
) {
    fun toStrContent() = StringBuilder().apply {
        h1?.let { append("# $it\n") }
        h2?.let { append("## $it\n") }
        desc?.let { append("> $it\n") }
        code?.let { append("`$it`\n") }
        post?.let { append("$it\n") }
        li.forEach {
            append("- $it")
        }
    }.toString()

    override fun toString(): String = toStrContent()
}
//endregion