package site.jiyang

import com.squareup.moshi.Moshi
import site.jiyang.model.config.Config
import java.io.BufferedReader
import java.io.File

class ConfigMgr private constructor() {

    private val config: Config = parse()

    private fun parse(): Config = File(configPath).let { f ->
        println("Config path: ${f.absolutePath}")
        val bufferedReader: BufferedReader = f.bufferedReader()
        val configJson = bufferedReader.use { it.readText() }
        Moshi.Builder().build().adapter(Config::class.java).fromJson(configJson)!!
    }

    companion object {
        private val mgr: ConfigMgr by lazy(LazyThreadSafetyMode.NONE) { ConfigMgr() }
        private var configPath: String? = null

        fun setConfigFilePath(configFilePath: String) {
            if (configPath != null) {
                throw IllegalStateException("setConfigFilePath can call only once")
            }
            configPath = configFilePath
        }

        fun config(): Config {
            if (configPath == null) {
                throw IllegalStateException("Must first call site.jiyang.ConfigMgr.setConfigPath")
            }
            return mgr.config
        }
    }
}