package site.jiyang.dao

import com.squareup.moshi.Moshi
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import site.jiyang.model.Issue
import site.jiyang.model.config.Config

class MySqlDaoImpl(config: Config) : IDao {

    init {
        Database.connect(
            "jdbc:mysql://${config.mysql.host}",
            driver = "com.mysql.jdbc.Driver",
            user = config.mysql.user,
            password = config.mysql.pass
        )
        transaction { SchemaUtils.create(Issues) }
    }

    override fun insert(issues: List<Issue>) {
        transaction {
            issues.forEach {
                site.jiyang.dao.Issue.new {
                    issueId = it.issueId
                    json = Moshi.Builder().build().adapter(Issue::class.java).toJson(it)
                }
            }
        }
    }

    override fun insert(issue: Issue) {
        transaction {
            site.jiyang.dao.Issue.new {
                issueId = issue.issueId
                json = Moshi.Builder().build().adapter(Issue::class.java).toJson(issue)
            }
        }
    }

    override fun exists(issueId: String): Boolean = !site.jiyang.dao.Issue.find { Issues.issueId eq issueId }.empty()
}