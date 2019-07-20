package site.jiyang.model

data class Issue(
    val count: Int,
    val crashNum: Int,
    val exceptionMessage: String,
    val exceptionName: String,
    val ftName: String,
    val imeiCount: Int,
    val issueId: String,
    val issueVersions: List<IssueVersion>,
    val keyStack: String,
    val lastestUploadTime: String,
    val processor: String,
    val status: Int,
    val tagInfoList: List<Any>,
    val version: String
)