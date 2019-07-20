package site.jiyang.dao

import com.squareup.moshi.Moshi
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import site.jiyang.model.config.Config
import site.jiyang.model.Issue as IssueModel

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

    override fun insert(issues: List<IssueModel>) {
        transaction {
            issues.forEach {
                Issue.new {
                    issueId = it.issueId
                    json = Moshi.Builder().build().adapter(IssueModel::class.java).toJson(it)
                }
            }
        }
    }

    override fun insert(issue: IssueModel) {
        transaction {
            Issue.new {
                issueId = issue.issueId
                json = Moshi.Builder().build().adapter(IssueModel::class.java).toJson(issue)
            }
        }
    }

    override fun exists(issue: IssueModel): Boolean {
        return transaction {
            val found = Issue.find { Issues.json like "%${issue.keyStack.replace("\t", "")}%" }
            !found.empty()
        }
    }
}