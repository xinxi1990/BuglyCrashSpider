package site.jiyang

import com.squareup.moshi.Moshi
import site.jiyang.model.Issue
import java.sql.Timestamp

/**
 * Create by StefanJi in 2019-07-21
 */

/**
 * Parse time string[Issue.lastestUploadTime](yyyy-mm-dd hh:MM:ss) to timestamp
 * @return timestamp
 */
fun String.toTimestamp(): Long = Timestamp.valueOf(this.substring(0, 19)).toInstant().epochSecond

fun Issue.toJsonString(): String = Moshi.Builder().build().adapter(Issue::class.java).toJson(this)

fun site.jiyang.dao.Issue.toIssueModel(): Issue? {
    return try {
        Moshi.Builder().build().adapter(Issue::class.java).fromJson(json)
    } catch (e: Exception) {
        println("[toIssueModel]: $e")
        null
    }
}