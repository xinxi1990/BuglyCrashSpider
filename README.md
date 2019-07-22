# Bugly Crash Spider

> 针对 Bugly 平台的 Crash 信息爬虫工具，运行时会分页扫描 Bugly 上的奔溃列表，遇到新出现的 Crash，就调用报警处理。

- [x] Crash 信息爬取
- [x] 新发生 Crash 报警
    - 扫描频率可自行配置(可通过 `crontab` 实现等)
    - 默认实现了发送到企业微信群机器人, 可自行扩展发送到其他 IM (Slack, 钉钉等)

- [x] 扫到的 Crash 信息会存的数据库, 以便下次扫描时过滤出新发生的 Crash. 默认实现了 MySql 存储,可自行扩展

> Bugly 的 token 和 cookie 一定时间之后会失效，这时需要重新到浏览器打开 Bugly 网页控制台获取新的 token 和 cookie，更新到配置文件中。

## 使用方法

### 使用 jar

#### 使用默认实现 

https://github.com/stefanJi/BuglyCrashSpider/releases

#### 自己打包:

```
./gradle clean build
```

再运行:

```
java -jar build/libs/BuglyCrashSpider.jar "config.json"
```

### 通过 gradle 执行

```
./gradlew run --args "config.json"
```

### 配置

> JSON 格式

|key|value type|note|
|:---|:---|:---|
|buGlyHost|`string`|Bugly的host地址 现在是`https://bugly.qq.com/v2`|
|auth.token|`string`|Bugly 请求响应 header 里的 X-token 对应的值|
|auth.cookie|`string`|Bugly 请求响应 header 里的 Set-cookie中 `bugly_session` 对应的值| 
|query.searchType|`string`|筛选的类型|
|query.exceptionTypeList|`string`|异常的类型|
|query.pid|`string`|默认为`1`|
|query.platformId|`string`| Android 为 `1`|
|query.sortOrder|`string`|排序方式 `asc` `desc`|
|query.status|`string`|筛选处理状态 0: 未处理|
|query.rows|`int`|每次请求的数据量 最大100|
|query.sortField|`string`|排序的key `uploadTime`: 按上报时间|
|query.appId|`string`|Bugly 中应用的 App ID|

以下为可选:

> 如果你使用现在的默认实现，则需要都填写

|key|value type|note|
|:---|:---|:---|
|qyWeChatBot.webHook|`string`|企业微信群机器人 hook 地址|
|mysql.host|`string`|MySQL 数据库 Host|
|mysql.user|`string`||
|mysql.pass|`string`||


<details>
<summary>配置文件模板</summary>

```json
{
  "buGlyHost": "https://bugly.qq.com/v2/issueList",
  "auth": {
    "token": "登录Bugly平台之后获取",
    "cookie": "登录Bugly平台之后获取"
  },
  "query": {
    "searchType": "errorType",
    "exceptionTypeList": "Crash,Native",
    "pid": "1",
    "platformId": "1",
    "sortOrder": "desc",
    "status": "0",
    "rows": 20,
    "sortField": "uploadTime",
    "appId": "Bugly上分配给App的id"
  },
  "qyWeChatBot": {
    "webHook": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=<群机器人的Key>"
  },
    "mysql": {
    "host": "localhost:3306/test?useSSL=true",
    "user": "root",
    "pass": "123456"
  }
}
```
</details>

### 扩展

- `IHandler` 报警处理, 现已提供 `QYWeChatHandler` 发送消息到企业微信群机器人
- `IDao` 数据持久化, 现已提供 `MySqlDaoImpl` 持久化到 MySQL
- `IRequester` 模拟请求 Bugly 接口, 现已提供 `OkHttpRequester` 通过 OkHttp 模拟请求
- `Filter` 过滤策略, 现已提供 `ExistsFilter`, 过滤类似的异常堆栈

```kotlin
class App {
    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            ConfigMgr.setConfigFilePath(parseCLIArg(*args))
            val config = ConfigMgr.config()
            val handler: IHandler = QYWeChatHandler(config.qyWeChatBot, config)
            val dao: IDao = MySqlDaoImpl(config)
            val requester: IRequester = OkHttpRequester()
            val filters: Array<Filter> = arrayOf(ExistsFilter())
            try {
                BuGlyCrashSpider(config, handler, requester, dao， filters).start()
            } catch (e: Exception) {
                handler.handleException(e)
            }
        }
     }
}
```