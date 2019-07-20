package site.jiyang.dao

import site.jiyang.model.Issue

interface IDao {
    fun insert(issues: List<Issue>)
    fun insert(issue: Issue)
    fun exists(issueId: String): Boolean
}