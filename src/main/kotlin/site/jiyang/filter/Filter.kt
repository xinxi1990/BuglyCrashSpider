package site.jiyang.filter

import site.jiyang.dao.IDao
import site.jiyang.model.Issue

/**
 * Create by StefanJi in 2019-07-21
 */
abstract class Filter {

    var dao: IDao? = null

    abstract fun applyFilter(issues: List<Issue>): List<Issue>
}