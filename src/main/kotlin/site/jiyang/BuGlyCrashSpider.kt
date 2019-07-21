/**
 * Create by StefanJi in 2019-07-19
 */

package site.jiyang

import com.squareup.moshi.Moshi
import site.jiyang.dao.IDao
import site.jiyang.filter.Filter
import site.jiyang.handlers.IHandler
import site.jiyang.model.BuGlyAuthFailedResp
import site.jiyang.model.BuGlyIssueResp
import site.jiyang.model.Issue
import site.jiyang.model.config.Config
import site.jiyang.requesters.IRequester
import java.util.*

class BuGlyCrashSpider(
    private val config: Config,
    private val handler: IHandler,
    private val requester: IRequester,
    private val dao: IDao,
    private val filters: Array<Filter>
) {
    companion object {
        private const val SLEEP_MILLIS = 2000L
    }

    init {
        filters.forEach { it.dao = dao }
    }

    /**
     * @return "[Config.buGlyHost]]?k1=v1&k2=v2"
     */
    private fun buildUrl(start: Int): String {
        return with(config.query) {
            mapOf(
                "start" to start,
                "searchType" to searchType,
                "exceptionTypeList" to exceptionTypeList,
                "pid" to pid,
                "platformId" to platformId,
                "sortOrder" to sortOrder,
                "status" to status,
                "rows" to rows,
                "sortField" to sortField,
                "appId" to appId
            )
        }.let { "${config.buGlyHost}/issueList?${it.map { item -> "${item.key}=${item.value}" }.joinToString("&")}" }
    }

    private fun buildHeader() = mapOf(
        "X-token" to config.auth.token,
        "Cookie" to "bugly_session=${config.auth.cookie}",
        "User-agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5)",
        "Content-Type" to "application/json;charset=utf-8",
        "Referer" to config.buGlyHost
    )

    private var page = 0
    private var nextStart = 0
    private var allCount = 0 /* 本次扫描总共获取到的数量 */
    private var newCount = 0 /* 本次扫描到的新增数量 */

    private var handlers = arrayOf(
        { json: String -> buildHandler(json, handleIssueResp) },
        { json: String -> buildHandler(json, handleAuthFailedResp) },
        { json: String -> buildHandler(json, handleUnknownResp) })

    fun start() {
        println("Start page: $page")
        val resp = requester.request(buildUrl(page), buildHeader())
        resp?.let {
            val iterator = handlers.iterator()
            var handled = false
            while (!handled) {
                handled = iterator.next()(it)()
            }

            if (nextStart > page) {
                println("Sleep $SLEEP_MILLIS")
                Thread.sleep(SLEEP_MILLIS)
                page = nextStart
                start()
            } else {
                println("Done")
                println("All fetch: $allCount")
                println("New fetch: $newCount")
                return
            }
        }
    }

    private inline fun <reified T, R> buildHandler(json: String, crossinline handler: (T) -> R): () -> Boolean {
        return {
            try {
                Moshi.Builder().build().adapter(T::class.java).fromJson(json)!!.let(handler)
                true
            } catch (ex: Exception) {
                println("Handle exception: $ex \n${Arrays.toString(ex.stackTrace)}")
                println("Exception resp: $json")
                false
            }
        }
    }

    private val handleIssueResp: (BuGlyIssueResp) -> Unit = { issueResp ->
        val issues = issueResp.ret.issueList
        val pageSize = issues.size
        println("Handle resp size: $pageSize")
        when {
            issues.isEmpty() -> println("Empty issues")
            else -> {
                var newIssues = issues
                filters.forEach { newIssues = it.applyFilter(newIssues) }

                newCount += newIssues.size
                if (newIssues.isNotEmpty()) {
                    insert(newIssues)
                    handler.handleIssuesResp(newIssues)
                }
                nextStart += pageSize
            }
        }
        allCount += pageSize
    }

    private val handleAuthFailedResp: (BuGlyAuthFailedResp) -> Unit = { resp ->
        println("Handle Auth failed: $resp")
        handler.handleAuthFailedResp(resp)
    }

    private val handleUnknownResp: (String) -> Unit = { json ->
        println("Handle unknown resp: $json")
        handler.handleUnknownResp(json)
    }

    private fun insert(issues: List<Issue>) {
        dao.insert(issues)
    }
}