package com.vicc.scrap.application

import com.vicc.scrap.domain.Product
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.Locale

interface ProductService {
    fun findById(id: String, locale: Locale): Product?
    fun searchProduct(productSearchParam: ProductSearchParam): List<Product>
    fun save(product: Product)

    fun upload(product: Product)

    fun saveExternalService(product: Product)
}

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    @Qualifier("godoProductRepositoryImpl") private val godoProductRepositoryImpl: ProductRepository,
    private val productTranslateService: ProductTranslateService,
    private val shoppingMallService: ShoppingMallService,
    private val shopService: ShopService
): ProductService {
    override fun findById(id: String, locale: Locale): Product? {
        return productRepository.getById(id, locale)
    }

    override fun searchProduct(productSearchParam: ProductSearchParam): List<Product> {
        return productRepository.search(productSearchParam)
    }

    override fun save(product: Product) {
        productRepository.save(product, productTranslateService.translateB2c(product))
    }

    override fun upload(product: Product) {
        shoppingMallService.save(product)
        productRepository.saveKr(product)
    }

    override fun saveExternalService(product: Product) {
        val godoProduct = godoProductRepositoryImpl.getById(product.id, Locale.KOREA)
        val prod = product.copy(alternativeId = godoProduct?.alternativeId)
        productRepository.saveKr(prod).also { godoProductRepositoryImpl.saveKr(prod).also {  } }
    }
}