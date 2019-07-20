package site.jiyang.model.config

data class Config(
    val auth: Auth,
    val buGlyHost: String,
    val mysql: Mysql,
    val query: Query,
    val qyWeChatBot: QyWeChatBot
)