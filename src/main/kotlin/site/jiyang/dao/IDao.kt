package site.jiyang.dao

import site.jiyang.model.Issue

interface IDao {
    fun insert(issues: List<Issue>)
    fun insert(issue: Issue)
    fun exists(issue: Issue): Boolean
    fun lastUploadIssue(): Issue?
}