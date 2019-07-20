@file:JvmName("App")

package site.jiyang

import site.jiyang.dao.IDao
import site.jiyang.dao.MySqlDaoImpl
import site.jiyang.handlers.IHandler
import site.jiyang.handlers.QYWeChatHandler
import site.jiyang.requesters.IRequester
import site.jiyang.requesters.OkHttpRequester
import java.util.*

class App {
    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            ConfigMgr.setConfigFilePath(parseCLIArg(*args))
            val config = ConfigMgr.config()
            val handler: IHandler = QYWeChatHandler(config.qyWeChatBot, config)
            val dao: IDao = MySqlDaoImpl(config)
            val requester: IRequester = OkHttpRequester()
            try {
                BuGlyCrashSpider(config, handler, requester, dao).start()
            } catch (e: Exception) {
                handler.handleException(e)
            }
        }

        private fun parseCLIArg(vararg args: String): String {
            println("args: ${Arrays.toString(args)}")
            if (args.isEmpty()) {
                throw IllegalStateException("Must pass config path arg")
            }
            if (args.size > 1) {
                throw IllegalStateException("Only support one position arg")
            }
            return args.first()
        }
    }
}
