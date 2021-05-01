package com.haero_kim.pickmeup.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * OpenGraph 프로토콜에 따라, HTML 에서 정규식을 통해 OG 태그를 찾아 값을 반환하는 함수를 포함하는 Object
 * - Coroutines Scope 내에서 호출해야 함
 */
object OpenGraphParser {
    suspend fun parse(url: String): Map<String, String> = withContext(Dispatchers.IO) {
        var connection: HttpsURLConnection? = null

        try {
            connection = URL(url).openConnection() as HttpsURLConnection
            connection.connect()

            val result = connection.inputStream?.run {
                val reader = BufferedReader(InputStreamReader(this))
                val buffer = StringBuffer()

                var line: String? = ""
                while (reader.readLine().also { line = it } != null) {
                    buffer.append(line)
                }
                buffer.toString()
            }
            parseOgTag(result ?: "")
        } catch (ex: Exception) {
            return@withContext mapOf<String, String>()
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 'og' 란 prefix 가 붙은 모든 meta 태그들의 content 를 파싱함
     * - ex) <meta property="og:type" content="article"> ==> Map<String, String>("type" to "article")
     */
    private fun parseOgTag(html: String): Map<String, String> {
        val opTags = mutableMapOf<String, String>()

        Regex("<meta property[^>]([^<]*)>").findAll(html).forEach {
            val metaProperty = it.groupValues.getOrNull(1) ?: ""
            Regex("\"og:(.*)\" content=\"(.*)\"").find(metaProperty)?.let {
                val ogType = it.groupValues.getOrNull(1)
                val content = it.groupValues.getOrNull(2)
                if (ogType != null && content != null) {
                    opTags[ogType] = content
                }
            }
        }
        return opTags
    }
}