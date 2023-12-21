package com.vicc.scrap.infra.api

import com.vicc.scrap.application.ProductService
import com.vicc.scrap.application.ScrapService
import com.vicc.scrap.domain.Product
import com.vicc.scrap.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class AdminController(
    private val scrapService: ScrapService,
    private val streamBridge: StreamBridge,
    private val productService: ProductService
) {
    @Value("\${vvic.categories}") lateinit var leafCategoryIds: List<String>

    @GetMapping("/save/{id}")
    fun save(@PathVariable id: String) {
        scrapService.productById(id).apply { productService.save(this) }

        productService.findById(id, Locale.KOREA)?.let {
            productService.upload(it)
            productService.saveExternalService(it)
        }
    }

    @GetMapping("/upload/{id}")
    fun upload(@PathVariable id: String) {
        productService.findById(id, Locale.KOREA)?.let { productService.upload(it) } ?: throw NullPointerException("product not exist")
    }

    @GetMapping("/upload/godo/{id}")
    fun uploadGodo(@PathVariable id: String) {
        productService.saveExternalService(productService.findById(id, Locale.KOREA)!!)
    }

    @GetMapping("/db/{id}", produces = ["application/json"])
    fun findById(@PathVariable id: String): Product? {
        return productService.findById(id, Locale.KOREA)
    }

    @GetMapping("/pub-category")
    fun pubCategory() {
        leafCategoryIds.forEach {
            (1..70).forEachIndexed { index, i ->
                logger().info("category $it page $i")
                scrapService.productIdsFromCategory(it, i).forEach {
                    streamBridge.send("vvicCrawlRequest", it)
                }
            }
        }
    }
}