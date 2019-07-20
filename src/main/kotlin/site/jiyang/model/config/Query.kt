package site.jiyang.model.config

data class Query(
    val appId: String,
    val exceptionTypeList: String,
    val pid: String,
    val platformId: String,
    val rows: Int,
    val searchType: String,
    val sortField: String,
    val sortOrder: String,
    val status: String
)