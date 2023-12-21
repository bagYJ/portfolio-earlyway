package com.vicc.scrap.infra.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vicc.scrap.application.ProductRepository
import com.vicc.scrap.application.ProductSearchParam
import com.vicc.scrap.domain.DuplicateException
import com.vicc.scrap.domain.Product
import com.vicc.scrap.logger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import java.util.*

@Service("godoProductRepositoryImpl")
class GodoProductRepositoryImpl(
    private val productRepository: ProductRepository
): ProductRepository {
    private val mapper = jacksonObjectMapper()

    private val restTemplate = RestTemplateBuilder()
        .rootUri("https://openhub.godo.co.kr")
        .build()

    private val GODO_PARTNER_KEY = "GODO_PARTNER_KEY"
    private val GODO_API_KEY = "GODO_API_KEY"

    fun goodsSearch(id: String): ResponseEntity<Map<*, *>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = Collections.singletonList(MediaType.APPLICATION_JSON)
        val requestMap: LinkedMultiValueMap<String, String> = LinkedMultiValueMap()
        requestMap["partner_key"] = GODO_PARTNER_KEY
        requestMap["key"] = GODO_API_KEY
        requestMap["goodsCd"] = id
        val formEntity = HttpEntity(requestMap, headers)

        return restTemplate.postForEntity("/godomall5/goods/Goods_Search.php", formEntity, Map::class.java)
    }

    fun getGoodsNo(id: String) {
        val response = goodsSearch(id)
        val body = response.body!! as Map<Any, Any>
        if (body.containsKey("return") && body["return"] is String) {
            null
        } else {
            val result = body["return"] as Map<Any, Any>
            if (!result.containsKey("goods_data")) {
                null
            } else {
                val godoProductId = ((body["return"] as Map<Any, Any>)["goods_data"] as Map<Any, Any>)["goodsNo"] as String
                if (godoProductId != null) {
                    throw DuplicateException("")
                }
            }
        }
    }

    override fun getById(id: String, locale: Locale): Product? {
        val response = goodsSearch(id)
        return if (!checkSuccess(response)) {
            throw RuntimeException("godomall get By Id failed")
        } else {
            val body = response.body!! as Map<Any, Any>
            if (body.containsKey("return") && body["return"] is String) {
                null
            } else {
                val result = body["return"] as Map<Any, Any>
                if (!result.containsKey("goods_data")) {
                    null
                } else {
                    val godoProductId = ((body["return"] as Map<Any, Any>)["goods_data"] as Map<Any, Any>)["goodsNo"] as String
                    productRepository.getById(id, Locale.KOREA)!!.copy(alternativeId = godoProductId)
                }
            }
        }
    }

    override fun save(product: Product, productKr: Product?) {
        TODO("Not yet implemented")
    }

    override fun saveKr(productKr: Product) {

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = Collections.singletonList(MediaType.APPLICATION_JSON)
        val requestMap: LinkedMultiValueMap<String, String> = LinkedMultiValueMap()
        requestMap["partner_key"] = GODO_PARTNER_KEY
        requestMap["key"] = GODO_API_KEY
        requestMap["data_url"] = "http://ew:ew@earlyway2.asuscomm.com/godo/${productKr.id}"
        val formEntity = HttpEntity(requestMap, headers)

        val response = if (productKr.alternativeId == null) {
            logger().info("register product at godo")
            restTemplate.postForEntity("/godomall5/goods/Goods_Insert.php", formEntity, Map::class.java)
        } else {
            logger().info("update product at godo")
//            requestMap["data_url"] = "http://ew:ew@earlyway2.asuscomm.com/godo/update/${productKr.id}"
//            restTemplate.postForEntity("/godomall5/goods/Goods_Update.php", formEntity, Map::class.java)
            throw DuplicateException("")
        }
        if(!checkSuccess(response)) {
            throw RuntimeException("product $productKr failed to save godo")
        }

        val body = response.body!! as Map<Any, Any>

        try {
            if (body.containsKey("return") && body["return"] is String) {
                null
            } else {
                val result = body["return"] as Map<Any, Any>
                val goodsNo = ((result["goods_data"] as Map<Any, Any>)["data"] as Map<Any, Any>)["goodsno"] as String
                productKr.copy(alternativeId = goodsNo)
            }
        }catch (e: Exception) {
            val errorMessage = mapper.writeValueAsString(body)
            logger().error(errorMessage)
            e.printStackTrace()
            throw Exception(errorMessage)
        }


    }

    override fun search(product: ProductSearchParam): List<Product> {
        TODO("Not yet implemented")
    }

    private fun checkSuccess(entity: ResponseEntity<Map<*, *>>): Boolean {
        val body = entity.body!! as Map<Any, Any>
        return if ((body["header"] as Map<Any, Any>)["code"] as String == "000") {
            true
        } else {
            logger().error(entity.body.toString())
            false
        }
    }

}