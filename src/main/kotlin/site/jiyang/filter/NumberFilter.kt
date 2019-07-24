package site.jiyang.filter

import site.jiyang.model.Issue

/**
 * Create by StefanJi in 2019-07-23
 * 过滤发生次数小于[filterCount]的 Crash
 */
class NumberFilter(private val filterCount: Int) : Filter() {
    override fun applyFilter(issues: List<Issue>): List<Issue> {
        return issues.filter { it.crashNum > filterCount }
    }
}