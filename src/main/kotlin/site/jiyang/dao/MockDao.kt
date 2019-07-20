package site.jiyang.dao

import site.jiyang.model.Issue

class MockDao : IDao {

    private val mockStorage = ArrayList<String>()

    override fun insert(issue: Issue) {
        mockStorage.add(issue.issueId)
    }

    override fun exists(issueId: String): Boolean = mockStorage.contains(issueId)

    override fun insert(issues: List<Issue>) {
        mockStorage.addAll(issues.map { it.issueId })
    }
}