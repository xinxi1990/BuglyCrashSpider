package site.jiyang.requesters

interface IRequester {
    fun request(url: String, headers: Map<String, String>): String?
}