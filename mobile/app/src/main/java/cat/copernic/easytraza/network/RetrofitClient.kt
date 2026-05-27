package cat.copernic.easytraza.network

import com.google.gson.GsonBuilder
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * Classe per a la gestió de RetrofitClient a l'aplicació EasyTraza.
 */
class RetrofitClient {

    companion object {

        private var currentIp: String = ""
        private var retrofit: Retrofit? = null

        private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

        private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        private val sslContext: SSLContext by lazy {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            sc
        }

        private val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore.getOrPut(url.host) { mutableListOf() }.addAll(cookies)
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val cookies = cookieStore[url.host] ?: return emptyList()
                    val valid = cookies.filter { it.expiresAt > System.currentTimeMillis() }
                    cookieStore[url.host] = valid.toMutableList()
                    return valid
                }
            })
            .build()

        fun getClient(ip: String): Retrofit {
            if (ip != currentIp || retrofit == null) {
                currentIp = ip
                val baseUrl = "https://$ip:8443/"
                val gson = GsonBuilder()
                    .setLenient()
                    .create()
                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit!!
        }

        fun clearSession() {
            cookieStore.clear()
        }
    }
}
