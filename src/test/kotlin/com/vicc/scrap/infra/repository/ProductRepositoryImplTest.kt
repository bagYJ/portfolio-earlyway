package com.vicc.scrap.infra.repository

import com.vicc.scrap.application.ProductSearchFilter
import com.vicc.scrap.application.ProductSearchParam
import com.vicc.scrap.domain.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import java.time.Duration
import java.util.*

class ProductRepositoryImplTest : FunSpec({

    val config = ClientConfiguration.builder().connectedTo("localhost:9200")
        .withConnectTimeout(Duration.ofSeconds(5))
        .withSocketTimeout(Duration.ofSeconds(10))
        .build()
    val esClient = RestClients.create(config).rest()

    val repository = ProductRepositoryImpl(esClient, mockk(relaxed = true))

    val productKr = GL_PRODUCT.copy(name = "한국용 상품")

    test("search") {
        repository.search(ProductSearchParam(filter = ProductSearchFilter(name = "한국용 상품"))).first() shouldBe productKr
    }

    test("getById") {
        repository.getById("test", Locale.KOREA) shouldBe productKr
    }

    test("save") {
        repository.save(GL_PRODUCT, productKr)

    }
})
