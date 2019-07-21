package site.jiyang.dao

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import site.jiyang.model.config.Config
import site.jiyang.toIssueModel
import site.jiyang.toJsonString
import site.jiyang.toTimestamp
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
                    uploadTime = it.lastestUploadTime.toTimestamp()
                    issueId = it.issueId
                    json = it.toJsonString()
                }
            }
        }
    }

    override fun insert(issue: IssueModel) {
        transaction {
            Issue.new {
                uploadTime = issue.lastestUploadTime.toTimestamp()
                issueId = issue.issueId
                json = issue.toJsonString()
            }
        }
    }

    override fun exists(issue: IssueModel): Boolean {
        return transaction {
            val found = Issue.find { Issues.json like "%${issue.keyStack.replace("\t", "")}%" }
                .filter { it.issueId != issue.issueId }
            found.isNotEmpty()
        }
    }

    override fun lastUploadIssue(): IssueModel? {
        return transaction {
            Issue.all().sortedBy { it.uploadTime }.reversed().firstOrNull()?.toIssueModel()
        }
    }
}