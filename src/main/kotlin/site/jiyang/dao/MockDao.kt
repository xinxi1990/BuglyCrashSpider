package site.jiyang.dao

import site.jiyang.model.Issue

class MockDao : IDao {
    override fun lastUploadIssue(): Issue? = null

    private val mockStorage = ArrayList<String>()

    override fun insert(issue: Issue) {
        mockStorage.add(issue.keyStack)
    }

    override fun exists(issue: Issue): Boolean = mockStorage.find { it.contains(issue.keyStack) }?.isNotEmpty() ?: false

    override fun insert(issues: List<Issue>) {
        mockStorage.addAll(issues.map { it.keyStack })
    }
}