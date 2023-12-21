package com.vicc.scrap.infra.repository

import com.vicc.scrap.domain.Shop
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe

class GodoShopRepositoryImplTest : FunSpec({
    val repository = GodoShopRepositoryImpl()
    test("save") {
        val shop = Shop(
            address = "중국 어딘가",
            phoneNumber = "123-456-788",
            name = "시스템 테스트 브랜드",
            popularity = "20000",
            messengerId = "test-test-test",
            origin = "",
            id = "999999",
            follower = "10",
            saleCount = "200",
            newProductCount = "33"
        )
        val after = repository.save(shop)

        after.alternativeId shouldNotBe null

    }
})
