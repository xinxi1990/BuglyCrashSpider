package site.jiyang.filter

import site.jiyang.model.Issue

/**
 * Create by StefanJi in 2019-07-21
 */
class ExistsFilter : Filter() {

    override fun applyFilter(issues: List<Issue>): List<Issue> {
        if (issues.isEmpty()) {
            return emptyList()
        }
        return issues.filterNot { dao?.exists(it) ?: false }
    }
}