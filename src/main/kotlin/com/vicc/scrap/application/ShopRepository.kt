package com.vicc.scrap.application

import com.vicc.scrap.domain.Shop

interface ShopRepository {
    fun byId(id: String): Shop?
    fun save(shop: Shop): Shop
}