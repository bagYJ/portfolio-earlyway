package com.vicc.scrap.application

import com.vicc.scrap.logger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

interface TranslateService {
    fun String.stripUnnecessaryWords(): String {
        val forbiddenFile = Scanner(File("/home/ew/apps/forbidden_cn.txt")).useDelimiter(",")
        val forbiddenWords: MutableList<String> = mutableListOf()
        while (forbiddenFile.hasNext()) {
            forbiddenWords.add(forbiddenFile.next())
        }
        var result = this
        for (item in forbiddenWords) {
            result = result.replace(item, "")
        }
        return result
    }
    fun String.forceTranslate(): String {
        val synonymFile = Scanner(File("/home/ew/apps/synonym_cn.txt"))
        val synonymWords: MutableMap<String, String> = mutableMapOf()
        while (synonymFile.hasNextLine()) {
            val synonym = synonymFile.nextLine().split(":")
            synonymWords[synonym[0]] = synonym[1]
        }
        var result = this
        for ((key, value) in synonymWords) {
            result = result.replace(key, value)
        }
        return result
    }
    fun forbiddenWord(text: String): String {
        var result = translate(text)
        val forbiddenFile = Scanner(File("/home/ew/apps/forbidden.txt")).useDelimiter(",")
        val forbiddenWords: MutableList<String> = mutableListOf()
        while (forbiddenFile.hasNext()) {
            forbiddenWords.add(forbiddenFile.next())
        }
        for (i in forbiddenWords.indices) {
            println("$result >> ${forbiddenWords[i]}")
            result = result.replace(forbiddenWords[i], "")
        }
        return result
    }
    fun synonymWord(text: String): String {
        var result = text
        val synonymFile = Scanner(File("/home/ew/apps/synonym.txt"))
        println(synonymFile)
        val synonymWords: MutableMap<String, String> = mutableMapOf()
        while (synonymFile.hasNextLine()) {
            val synonym = synonymFile.nextLine().split(":")
            synonymWords[synonym[0]] = synonym[1]
        }
        for ((key, value) in synonymWords) {
            result = result.replace(key, value)
        }
        return result
    }
    fun translate(text: String): String
}

@Service
class PapagoTranslateService : TranslateService {
    private var dict = mutableMapOf<String, String>()


    private val restTemplate = RestTemplateBuilder()
        .rootUri("https://naveropenapi.apigw.ntruss.com")
        .defaultHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .defaultHeader("X-NCP-APIGW-API-KEY-ID", "X-NCP-APIGW-API-KEY-ID")
        .defaultHeader("X-NCP-APIGW-API-KEY", "X-NCP-APIGW-API-KEY")
        .build()

    override fun translate(text: String): String {
        if(text.trim().isEmpty()) return text
        val target = text.stripUnnecessaryWords().forceTranslate()
//        logger().info("$target")
        val translated = fromDict(target) ?: callPapago(target)
        store(target, translated)
//        logger().info("$translated")
        return synonymWord(translated)
    }

    private fun callPapago(text: String): String {
        try {
            val httpEntity = HttpEntity(mapOf(
                "source" to "zh-CN",
                "target" to "ko",
                "text" to text,
            ))
            val response = restTemplate.postForEntity("/nmt/v1/translation", "source=zh-CN&target=ko&text=$text", Map::class.java)
            return if (response.statusCode.is2xxSuccessful && response.body != null) {
                ((response.body!!["message"] as Map<String, String>)["result"] as Map<String, String>)["translatedText"] as String
            } else {
                throw RuntimeException("papago 오류 발생. 오류코드: ${response.statusCode.value()} 오류 메시지: ${response.body!!["errorMessage"]}")
            }
        }catch (e: Exception) {
            logger().error("call papago exception : $text")
            e.printStackTrace()
            return text
        }
    }

    private fun fromDict(text: String): String? {
        return dict[text]
    }
    private fun store(originText: String, translated: String) {
        if (originText.length < 5) {
            dict.put(originText, translated)
        }
    }
}