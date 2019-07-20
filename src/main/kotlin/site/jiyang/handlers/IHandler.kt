package site.jiyang.handlers

import site.jiyang.model.BuGlyAuthFailedResp
import site.jiyang.model.Issue
import java.lang.Exception

interface IHandler {

    fun handleIssuesResp(issues: List<Issue>)

    fun handleAuthFailedResp(authFailedResp: BuGlyAuthFailedResp)

    fun handleUnknownResp(unknownResp: String)

    fun handleException(e: Exception)
}