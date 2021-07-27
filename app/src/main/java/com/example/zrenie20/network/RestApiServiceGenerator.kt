package com.example.zrenie20.network

import com.betcityru.android.betcityru.network.AppHttpLoggingInterceptor
import com.example.zrenie20.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

//private var BASE_URL = "https://app.tex-expert.ru/"
private var BASE_URL = "https://zrenie2-dev.site:3002/api/"

val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)

private val gson = GsonBuilder()
        .setLenient()
        .create()

private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(getUnsafeOkHttpClient())

private val retrofit = builder.build()

fun <S> createService(
        serviceClass: Class<S>): S {
    return retrofit.create(serviceClass)
}

fun changeBaseUrl(url: String) {
    BASE_URL = url
}

fun getUnsafeOkHttpClient(): OkHttpClient {
    try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }
        })

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        val builder = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(AppHttpLoggingInterceptor())

        builder.connectTimeout(1, TimeUnit.MINUTES)
        builder.readTimeout(1, TimeUnit.MINUTES)
        builder.writeTimeout(1, TimeUnit.MINUTES)

        builder.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val original = chain.request()
                val originalHttpUrl = original.url()

                val urlBuilder = originalHttpUrl.newBuilder()

                  val url = urlBuilder.build()

                    val authorisedRequest = original
                            .newBuilder()
                            .url(url)
                            .build()

                    return chain.proceed(authorisedRequest)
                }
        })

        builder.sslSocketFactory(sslSocketFactory)
        builder.hostnameVerifier { _, _ -> true }

        return builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }

}
