package com.github.krzsernik.ytmusicdownloader

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class Request @Throws(IOException::class)
internal constructor(private val m_url: String, method: String) {
    private val m_cookieManager = CookieManager()
    val connection: HttpsURLConnection

    var cookies: List<HttpCookie>
        get() {
            if (m_cookieManager.cookieStore.cookies.isEmpty()) {
                val cookiesHeader = connection.getHeaderField("Set-Cookie")
                val cookies = HttpCookie.parse(cookiesHeader)
                cookies.forEach { cookie -> m_cookieManager.cookieStore.add(null, cookie) }
            }

            return m_cookieManager.cookieStore.cookies
        }
        set(cookies) {
            val sb = StringBuilder()
            cookies.forEach { cookie -> sb.append("$cookie; ") }
            connection.setRequestProperty("Cookie", sb.toString())
        }

    val content: String
        @Throws(IOException::class)
        get() {
            val inp = BufferedReader(InputStreamReader(connection.inputStream))
            var inputLine = inp.readLine()
            val content = StringBuilder()
            while (inputLine != null) {
                content.append(inputLine)

                inputLine = inp.readLine()
            }
            inp.close()

            return content.toString()
        }

    init {
        val urlObj = URL(m_url)
        connection = urlObj.openConnection() as HttpsURLConnection
        connection.connectTimeout = 5000
        connection.requestMethod = method
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36")
        connection.doInput = true
    }

    fun setRequestHeader(name: String, value: String) {
        connection.setRequestProperty(name, value)
    }

    @Throws(IOException::class)
    fun setData(output: String) {
        connection.doOutput = true

        val os = connection.outputStream
        os.write(output.toByteArray(charset("UTF-8")))
    }

    @Throws(IOException::class)
    fun send(): Int {

        return connection.responseCode
    }

    fun getHeader(name: String): String {
        return connection.getHeaderField(name)
    }

    fun close() {
        connection.disconnect()
    }
}
