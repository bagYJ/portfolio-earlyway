package com.vicc.scrap.infra.eventbus

import com.vicc.scrap.application.JsoupScrapServiceImpl
import com.vicc.scrap.application.ProductService
import com.vicc.scrap.application.ScrapService
import com.vicc.scrap.domain.Product
import com.vicc.scrap.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.messaging.Message
import java.util.*
import java.util.function.Consumer

@Configuration
@Profile("consumer")
class VvicProductEventHandler(
    private val scrapService: ScrapService,
    private val productService: ProductService
) {
    @Bean
    fun scrapProduct() = java.util.function.Function<Message<String>, String> {
        logger().info(it.payload)
        val id = it.payload

        return@Function try {
            scrapService.productById(id).apply { productService.save(this) }
            id
        }catch (e: Exception) {
            logger().error("scrap error id: $id")
            e.printStackTrace()
            null
        }
    }

    @Bean
    fun indexProduct() = Consumer<Message<String>> {

        productService.findById(it.payload, Locale.KOREA)?.let {p ->
            productService.upload(p)
            productService.saveExternalService(p)
        }
    }
}