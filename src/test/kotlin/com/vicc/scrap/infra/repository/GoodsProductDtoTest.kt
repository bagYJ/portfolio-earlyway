package com.vicc.scrap.infra.repository

import com.vicc.scrap.domain.Product
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class GoodsProductDtoTest: FunSpec({
    test("상품명") {
        val product = mockk<Product>(relaxed = true) {
            every { categoryKr } returns mockk(relaxed = true) {
                every { fullName } returns emptyList()
            }
            every { name } returns "상품명"
            every { shopStyleNumber } returns "111"
        }
        val name = GoodsData.productName(product)
        name shouldBe product.shopStyleNumber + " " + product.name
    }
})