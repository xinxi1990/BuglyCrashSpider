package site.jiyang.model

data class Ret(
    val appId: String,
    val issueList: List<Issue>,
    val numFound: Int,
    val platformId: String
)