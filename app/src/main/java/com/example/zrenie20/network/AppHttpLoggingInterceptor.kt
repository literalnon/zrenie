package com.betcityru.android.betcityru.network

import android.util.Log
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class AppHttpLoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val networkLog = StringBuilder()

        val request = chain.request()

        val requestBody = request.body()

        networkLog.append(request.method())
        networkLog.append("\n")

        try {
            //networkLog.append(Gson().toJson(request.body()))
            //networkLog.append("\n")
        } catch (e: Exception) {
            //networkLog.append(e.message)
            //networkLog.append("\n")
        }

        var buffer = Buffer()
        requestBody?.writeTo(buffer)

        var charset: Charset? = UTF8
        var contentType = requestBody?.contentType()

        if (contentType != null) {
            charset = contentType.charset(UTF8)
        }

        /*if (isPlaintext(buffer)) {
            Log.e("interseptor", buffer.readString(charset!!))
        }*/

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            networkLog.append("<-- HTTP FAILED: $e")
            networkLog.append("\n")
            networkLog.append("CANCEL: ${request.url().url()}")
            networkLog.append("\n")
            /*AppLogger.log(AppLogObject(
                    type = LoggerType.NETWORK_REQUEST_TYPE,
                    tag = LogTag.NETWORK_REQUEST_TAG,
                    msg = "CANCEL: ${networkLog.method} : ${request.url().url()} : ${networkLog.responseCode} : ${networkLog.responseRequestUrl}",
                    networkLog = networkLog
            ))*/
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()

        networkLog.append(response.message())
        networkLog.append("\n")
        networkLog.append(response.code().toString())
        networkLog.append("\n")
        networkLog.append(response.request().url().toString())
        networkLog.append("\n")
        networkLog.append("$tookMs ms")
        networkLog.append("\n")

        val source = responseBody.source()
        source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.

        buffer = source.buffer()

        if (contentLength != 0L) {
            networkLog.append(buffer.clone().readUtf8())
            networkLog.append("\n")
        }

        /*AppLogger.log(AppLogObject(
                type = LoggerType.NETWORK_REQUEST_TYPE,
                tag = LogTag.NETWORK_REQUEST_TAG,
                msg = "${networkLog.method} : ${networkLog.responseCode} : ${networkLog.responseRequestUrl}",
                networkLog = networkLog
        ))*/

        lastStr = networkLog.toString()

        return response
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        var lastStr = ""
        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        /*internal fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size < 64) buffer.size else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }

        }*/
    }
}