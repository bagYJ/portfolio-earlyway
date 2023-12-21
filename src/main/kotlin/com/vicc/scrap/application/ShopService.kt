package com.vicc.scrap.application

import com.vicc.scrap.domain.Shop
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface ShopService {
    fun byId(id: String): Shop?
    fun save(shop: Shop): Shop
}

@Service
class ShopServiceImpl(
    private val shopRepositoryImpl: ShopRepository,
    @Qualifier("godoShopRepositoryImpl") private val godoShopRepositoryImpl: ShopRepository
): ShopService {
    override fun byId(id: String): Shop? {
        return shopRepositoryImpl.byId(id)
    }

    override fun save(shop: Shop): Shop {
        return godoShopRepositoryImpl.save(shop).let { shopRepositoryImpl.save(it) }
    }
}