package com.vicc.scrap.application

import com.vicc.scrap.domain.Product
import java.util.Locale

interface ProductRepository {
    fun getById(id: String, locale: Locale): Product?

    fun save(product: Product, productKr: Product?)
    fun saveKr(productKr: Product)
    fun search(product: ProductSearchParam): List<Product>
}

data class ProductSearchParam(
    val size: Int = 20,
    val filter: ProductSearchFilter,
    val locale: Locale? = Locale.KOREA
)

data class ProductSearchFilter(
    val id: String? = null,
    val name: String? = null,
)