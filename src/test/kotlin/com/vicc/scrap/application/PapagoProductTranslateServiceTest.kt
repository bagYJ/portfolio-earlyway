package com.vicc.scrap.application

import com.vicc.scrap.domain.Price
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal

class PapagoProductTranslateServiceTest : FunSpec({

    val service = PapagoProductTranslateService(
        mockk(relaxed = true), mockk(relaxed = true) { every { cny() } returns BigDecimal(166.4) }
    )
    test("price") {
        val price = service.price(Price(from = BigDecimal(24)))
        price.from shouldBe BigDecimal(4000)
    }
})
