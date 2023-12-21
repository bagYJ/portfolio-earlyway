package com.vicc.scrap.domain

import java.math.BigDecimal

data class Product(
    val id: String,
    val alternativeId: String? = null,
    val name: String,
    var images: List<String>,
    val colors: List<Option>,
    val shopStyleNumber: String,
    val sizes: List<Option>,
    val linkUrl: String,
    val shop: Shop,
    val price: Price,
    val salePrice: Price? = null,
    val buyPrice: Price? = null,
    val productMeta: ProductMeta,
    val categoryCn: Category? = null,
    val categoryKr: Category? = null,
    val attribute: List<Attribute> = emptyList(),
    var description: Description? = null,
    val sku: List<Sku> = emptyList()
)

data class Option(
    val name: String,
    val imageLink: String? = null
)

data class Shop(
    val address: String, //주소
    val phoneNumber: String, // 전화번호
    val name: String, //상점명
    val popularity: String? = null, //순위
    val messengerId: String? = null, //위챗
    val origin: String? = null, //기원
    val id: String? = null, // 상점 id
    val alternativeId: String? = null, // 외부 저장소 id
    val follower: String? = null, // 팬 수
    val saleCount: String? = null, // 판매수
    val newProductCount: String? = null, // 새 상품 수
    val selfPhotoRate: String? = null, // 상품 실제 촬영률
    val refundRate: String? = null, // 반품 성공률
    val qualityConfirmRate: String? = null, // 품질 합격률
    val runningYears: String? = null, // 운영 기간
    val deliveryAccuracyRate: String? = null // 배송 정확도
)

data class Price(
    val from: BigDecimal,
    val to: BigDecimal? = null
)

data class ProductMeta(
    val releaseDate: String? = null,
    val saleCount: Long? = null,
    val uploadCount: Long? = null,
    val wishCount: Long? = null
)

data class Category(
    val fullName: List<String>,
    val fullId: List<String>
)

data class Attribute(
    val key: String,
    val value: String
)

data class Description(
//    val images: List<String>,
    var rows: MutableList<DescriptionRow>
) {
    fun push(type: DescriptionRow.RowTypes, row: String) {
        if(row.isBlank()) return

        if(type == DescriptionRow.RowTypes.Image) {
            val descriptionRow = DescriptionRow(type,
                if (row.startsWith("https")) {
                    row
                } else {
                    "https:" + row
                })
            if(rows.contains(descriptionRow)) return
            rows.add(descriptionRow)
        }else {
            rows.add(DescriptionRow(type, row))
        }
    }

    fun toHtml(): String {
        val imageHtml = rows.map {
            when (it.rowType) {
                DescriptionRow.RowTypes.String -> "<p>${it.row}</p><br/>"
                DescriptionRow.RowTypes.Image -> "<img src=\"https://img.globird.co.kr${it.row}\" width=\"960\">"
            }
        }.joinToString(separator = "") { it }
        return """
            <div style="text-align:center;">
            <img src="https://earlyway.co.kr/data/skin/front/mo_designart/img/notice_1.jpg" width="960" >
            <img src="https://earlyway.co.kr/data/skin/front/mo_designart/img/notice_2.jpg" width="960">
            $imageHtml
            <img src="https://earlyway.co.kr/data/skin/front/mo_designart/img/notice_3.jpg" width="960">
            <img src="https://earlyway.co.kr/data/skin/front/mo_designart/img/notice_4.jpg" width="960">
            </div>
        """.trimIndent().replace("\n", "")
    }
}

data class DescriptionRow(
    val rowType: RowTypes,
    val row: String
) {
    enum class RowTypes {
        String, Image
    }

    fun getRow(decorator: String? = null): String {
        return when (rowType) {
            RowTypes.String -> row
            RowTypes.Image -> row
        }
    }
}

data class Sku(
    val color: Option?,
    val size: Option?,
    val orderAble: Boolean,
    val price: Price
)