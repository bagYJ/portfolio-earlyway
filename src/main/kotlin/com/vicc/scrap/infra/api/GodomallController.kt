package com.vicc.scrap.infra.api

import com.ctc.wstx.api.WstxOutputProperties
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.vicc.scrap.application.ProductService
import com.vicc.scrap.application.ShopService
import com.vicc.scrap.infra.repository.GodoProduct
import com.vicc.scrap.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class GodomallController(
    private val productService: ProductService,
    private val shopService: ShopService
) {
    @GetMapping("/godo/{id}", produces = [MediaType.APPLICATION_XML_VALUE])
    fun godoMapping(@PathVariable id: String): GodoProduct {
        var product = productService.findById(id, Locale.KOREA)!!
        val shop = shopService.save(product.shop)

        logger().info("godoMapping - shop : $shop")

        product = product.copy(shop = shop)
        return GodoProduct.from(product)
    }
//    @GetMapping("/godo/update/{id}", produces = [MediaType.APPLICATION_XML_VALUE])
//    fun godoUpdateMapping(@PathVariable id: String): GodoUpdateProduct {
//        val product = productService.findById(id, Locale.KOREA)
//        return GodoProduct.from(productService.findById(id, Locale.KOREA)!!)
//    }
}

@Configuration
class XmlConfig {
    @Bean
    fun mappingJackson2XmlHttpMessageConverter(builder: Jackson2ObjectMapperBuilder): MappingJackson2XmlHttpMessageConverter {
        val xmlMapper = builder.createXmlMapper(true).build<XmlMapper>()
        xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
        xmlMapper.factory.xmlOutputFactory.setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true)
        return MappingJackson2XmlHttpMessageConverter(xmlMapper)
    }
}