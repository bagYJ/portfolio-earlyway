package com.vicc.scrap.infra.repository

import com.vicc.scrap.application.ShopRepository
import com.vicc.scrap.domain.Shop
import com.vicc.scrap.logger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.stereotype.Service

@Service
class GodoShopRepositoryImpl(
): ShopRepository {
    private val headers = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }
    private val restTemplate =  RestTemplateBuilder().rootUri("https://earlyway.co.kr").build()

    override fun byId(id: String): Shop? {
        TODO("Not yet implemented")
    }

    override fun save(shop: Shop): Shop {
        val request = HttpEntity(listOf(GodoShop.from(shop), GodoShop.from(shop)) , headers)

        val response = restTemplate.postForEntity(
            "/main/test.php",
            request,
            Map::class.java
        )
        logger().info("브랜드 저장 = ${shop}")
        val godoMallBrandId = if (response.hasBody()) {
            val body = response.body!!
            val result = body["result"] as List<Map<String, String>>
            result.firstOrNull()?.get("cateCd") ?: ""
        } else {
            logger().error("브랜드 등록 실패 = ${shop.toString()}")
            ""
        }

        return shop.copy(alternativeId = godoMallBrandId)
    }

    data class GodoShop(
        val name: String,
        val address: String?,
        val productCount: Int?,
        val phoneNumber: String?,
        val popularity: Int?,
        val messengerId: String?,
        val origin: String?,
        val alternativeId: String?,
        val follower: Int?,
        val saleCount: Int?,
        val newProductCount: Int?,
        val grade: String?,
        val selfPhotoRate: String?,
        val refundRate: String?,
        val qualityConfirmRate: String?,
        val runningYears: Int?,
        val deliveryAccuracyRate: String?,
        val cateImg: String? = null,
        val cateImgMobileFl: String? = "y",
        val cateOverImg: String? = null,
        val cateOnlyAdultFl: String? = "n",
        val cateOnlyAdultDisplayFl: String? = "y",
        val cateOnlyAdultSubFl: String? = "n",
        val pcThemeCd: String? = null,
        val mobileThemeCd: String? = null,
        val sortType: String? = null,
        val sortAutoFl: String? = "y",
        val recomSubFl: String? = "n",
        val recomDisplayFl: String? = "y",
        val recomDisplayMobileFl: String? = "y",
        val recomSortType: String? = null,
        val recomSortAutoFl: String? = "y",
        val recomPcThemeCd: String? = null,
        val recomMobileThemeCd: String? = null,
        val recomGoodsNo: String? = null,
        val seoTagFl: String? = "n",
        val seoTagSno: String? = null,
        val cateHtml1: String? = null,
        val cateHtml2: String? = null,
        val cateHtml3: String? = null,
        val cateHtml1Mobile: String? = null,
        val cateHtml2Mobile: String? = null,
        val cateHtml3Mobile: String? = null
    ) {
        companion object {
            private fun stringToInto(str: String?): Int? {
                return if(str.isNullOrBlank()) {
                    null
                }else {
                    str.toInt()
                }
            }
            fun from(shop: Shop): GodoShop {
                return GodoShop(
                    name = shop.name,
                    address = shop.address,
                    productCount = stringToInto(shop.newProductCount),
                    phoneNumber = shop.phoneNumber,
                    popularity = stringToInto(shop.popularity),
                    messengerId = shop.messengerId,
                    origin = "중국",
                    alternativeId = shop.id,
                    follower = stringToInto(shop.follower),
                    saleCount = stringToInto(shop.saleCount),
                    newProductCount = stringToInto(shop.newProductCount),
                    grade = "M", // TODO 작업 필요
                    selfPhotoRate = shop.selfPhotoRate,
                    refundRate = shop.refundRate,
                    qualityConfirmRate = shop.qualityConfirmRate,
                    runningYears = 1, // TODO 작업 필요
                    deliveryAccuracyRate = shop.deliveryAccuracyRate

                )
            }
        }
    }

}