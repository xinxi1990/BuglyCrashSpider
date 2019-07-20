package site.jiyang.requesters

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

class OkHttpRequester : IRequester {

    private val client: OkHttpClient by lazy(LazyThreadSafetyMode.NONE) {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .addNetworkInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val resp = chain.request()
                    println(resp.url)
                    return chain.proceed(resp)
                }
            })
            .build()
    }

    override fun request(url: String, headers: Map<String, String>): String? {
        val request = Request.Builder()
            .url(url)
            .get()
            .apply { headers.forEach { k, v -> addHeader(k, v) } }
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }
}