package com.vicc.scrap.application

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.bigdecimal.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class DunamuCurrencyServiceTest : FunSpec({

    val service = DunamuCurrencyService()
    test("cny") {
        service.cny() shouldBeGreaterThan BigDecimal.valueOf(0)
    }
})
