package com.vicc.scrap.application

import com.vicc.scrap.logger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal

interface CurrencyService {
    fun cny(): BigDecimal
}

@Service
class FixedCurrencyService: CurrencyService {
    override fun cny(): BigDecimal {
        return BigDecimal.valueOf(190L)
    }
}


//@Service
open class DunamuCurrencyService: CurrencyService {
    private val restTemplate =  RestTemplateBuilder().rootUri("https://quotation-api-cdn.dunamu.com").build()

    @Cacheable("cny")
    override fun cny(): BigDecimal {
        val response = restTemplate.getForEntity("/v1/forex/recent?codes=FRX.KRWCNY", List::class.java)
        return BigDecimal.valueOf((response.body!![0] as Map<String, Any>)["highPrice"] as Double + 5)
    }

    @CacheEvict(value = ["cny"], allEntries = true)
    @Scheduled(fixedDelayString = "3600000")
    open fun evictCache() {
        logger().info("cache evicted")
    }

}