package com.vicc.scrap.infra.repository

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.annotation.*
import com.vicc.scrap.domain.Product
import java.math.BigDecimal
import java.time.OffsetDateTime

data class StepData (
    @JacksonXmlCData
    val infoTitle: String? = null,
    @JacksonXmlCData
    val infoValue: String? = null,
    @JacksonXmlProperty(isAttribute = true)
    val idx: Int? = null,
    @JacksonXmlCData
    val text: String? = null,
)

data class GoodsMustInfoData (
    val stepData: List<StepData>? = null,
    @JacksonXmlProperty(isAttribute = true)
    val idx: Int? = null,
    @JacksonXmlCData
    val text: String? = null,
)

data class OptionData (
    val optionNo: Int? = null,
    @JacksonXmlCData
    val optionValue1: String? = null,
    @JacksonXmlCData
    val optionValue2: String? = null,
//    @JacksonXmlCData
//    val optionValue3: String? = null,
//    @JacksonXmlCData
//    val optionValue4: String? = null,
//    @JacksonXmlCData
//    val optionValue5: String? = null,
    @JacksonXmlCData
    val optionPrice: BigDecimal? = null,
    @JacksonXmlCData
    val optionViewFl: String? = "y",
    @JacksonXmlCData
    val optionSellFl: String? = "y",
    @JacksonXmlCData
    val optionCode: String? = null,
    val stockCnt: Long? = 100000,
    @JacksonXmlProperty(isAttribute = true)
    val idx: Int? = null,
)

data class TextOptionData (
    val optionName: String? = null,
    val mustFl: String? = null,
    val addPrice: BigDecimal? = null,
    val inputLimit: Int? = null,
    val idx: Int? = null,
    val text: String? = null,
)

data class ImageData (
    @JacksonXmlProperty(isAttribute = true)
    val idx: Int? = null,
    @JacksonXmlCData
    @JacksonXmlText
    val text: String? = null,
)
data class GoodsData (
    @JacksonXmlProperty(isAttribute = true)
    val idx: Int = 1,
    @JacksonXmlCData
    val cateCd: String,
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    val goodsNo: String? = null,
    @JacksonXmlCData
    val allCateCd: String, //연결 카테고리 코드 * 구분자 : |
    val goodsDisplayFl: String = "y",
    val goodsDisplayMobileFl: String = "y",
    val goodsSellFl: String = "y",
    val goodsSellMobileFl: String = "y",
    @JacksonXmlCData
    val goodsCd: Any? = null,
    val goodsNmFl: String = "d",
    @JacksonXmlCData
    val goodsNm: String,
    val goodsNmMain: Any? = null,
    val goodsNmList: Any? = null,
    val goodsNmDetail: Any? = null,
    val goodsSearchWord: Any? = null,
    val goodsOpenDt: OffsetDateTime? = null,
    val goodsState: String = "n",
    @JacksonXmlCData
    val goodsColor: String? = null,
    @JacksonXmlCData
    val imageStorage: String = "https://img.globird.co.kr",
    val payLimitFl: String = "n",
    val payLimit: String? = null,
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    val brandCd: String? = null,
    val makerNm: Any? = null,
    val originNm: String? = "중국",
    val goodsModelNo: Any? = null,
    val makeYmd: String? = null,
    @JacksonXmlCData
    val launchYmd: String? = null,
    val effectiveStartYmd: OffsetDateTime? = null,
    val effectiveEndYmd: OffsetDateTime? = null,
    val goodsPermission: String = "all",
    val goodsPermissionGroup: Int? = null,
    val onlyAdultFl: String? = "n",
    val goodsMustInfoData: List<GoodsMustInfoData>? = null,
    val taxFreeFl: String = "t",
    val taxPercent: BigDecimal? = null,
    val goodsWeight: BigDecimal? = null,
    val totalStock: Int? = null,
    val stockFl: String = "n",
    val soldOutFl: String = "n",
    val salesUnit: Int? = 1,
    val fixedOrderCnt: String? = null,
    val minOrderCnt: Int? = 1,
    val maxOrderCnt: Int? = 0,
    val salesStartYmd: String? = null,
    val salesEndYmd: String? = null,
    val mileageFl: String = "c",
    val mileageGoods: BigDecimal? = null,
    val mileageGoodsUnit: String? = null,
    val goodsDiscountFl: String = "n",
    val goodsDiscount: Int? = null,
    val goodsDiscountUnit: String? = null,
    @JacksonXmlCData
    val fixedPrice: BigDecimal? = null,
    @JacksonXmlCData
    val costPrice: BigDecimal? = null,
    val goodsPriceString: Any? = null,
    @JacksonXmlCData
    val goodsPrice: BigDecimal? = null,
    val optionFl: String, // optionFl y | n
    val optionDisplayFl: String? = null,
    @JacksonXmlCData
    val optionName: String? = null,
    @JacksonXmlCData
    @JacksonXmlElementWrapper(useWrapping = false)
    val optionData: List<OptionData>? = null,
    val optionTextFl: String, // 텍스트옵션여부  y | n
    val addGoodsFl: String = "n", // 추가 상품 여부
    val textOptionData: List<TextOptionData>? = null,
    @JacksonXmlElementWrapper(useWrapping = false)
    val detailImageData: List<ImageData>? = null,
    @JacksonXmlElementWrapper(useWrapping = false)
    val listImageData: List<ImageData>? = null,
    @JacksonXmlElementWrapper(useWrapping = false)
    val magnifyImageData: List<ImageData>? = null,
    @JacksonXmlElementWrapper(useWrapping = false)
    val mainImageData: List<ImageData>? = null,
    val shortDescription: Any? = null,
    @JacksonXmlCData
    val goodsDescription: String? = null,
    val goodsDescriptionMobile: String? = null,
    val deliverySno: Int,
    val relationFl: String = "a", //관련상품 설정 ( n=사용안함, a=자동(동일카테고리 상품 무작위), m=직접선택)
    val relationSameFl: String? = null,
    val relationCnt: Int? = null,
    val relationGoodsNo: String? = null,
    val relationGoodsDate: Any? = null,
    val goodsIconStartYmd: String? = null,
    val goodsIconEndYmd: String? = null,
    val goodsIconCdPeriod: Any? = null,
    val goodsIconCd: String? = null,
    val imgDetailViewFl: String = "n",
    val externalVideoFl: String = "n",
    val externalVideoUrl: Any? = null,
    val externalVideoWidth: Int? = null,
    val externalVideoHeight: Int? = null,
    val detailInfoDelivery: String = "002001", // 배송안내 (0=사용안함)
    val detailInfoAS: String = "003001", // AS안내 (0=사용안함)
    val detailInfoRefund: String = "004001", //환불안내 (0=사용안함)
    val detailInfoExchange: String = "005001", // 교환안내 (0=사용안함)
    val memo: Any? = null,
    val scmNo: String = "1",
    val restockFl: String = "y",
    val qrCodeFl: String? = null,
    val text: String? = null,
) {

    companion object {
        fun productName(product: Product, decorate: Boolean? = false): String {
//            var categoryName = product.categoryKr?.fullName?.let {
//                if(it.isNotEmpty()) {
//                    it.last()
//                }else {
//                    ""
//                }
//            } ?: ""
//            if(decorate == true) {
//                categoryName = """
//                    <strong style="color:#00C73C;">$categoryName</strong>
//                """.trimIndent()
//            }
//            return categoryName + " " + product.name
            return product.name
        }
        fun from(product: Product): GoodsData {
            val imageDomain = "https://img.globird.co.kr"
            val firstSku = product.sku.firstOrNull()

            return GoodsData(
                goodsNm = productName(product, true),
                goodsNmMain = productName(product, true),
                goodsNmList = productName(product, true),
                goodsNmDetail = productName(product, true),
                goodsCd = product.id,
                goodsNo = product.alternativeId,
                cateCd = product.categoryKr!!.fullId.joinToString(separator = "") { it },
//                    goodsColor = product.colors.joinToString(separator = "^|^") { it.name },
                brandCd = product.shop.alternativeId,
                launchYmd = product.productMeta.releaseDate,
                allCateCd = product.categoryKr.fullId.joinToString(separator = "") { it },
                deliverySno = 1,
                addGoodsFl = "y",
                optionFl = if (product.sku.isEmpty()) "n" else "y",
                optionDisplayFl = if (product.sku.isEmpty()) null else "d",
                fixedOrderCnt = if(product.sku.isEmpty()) "goods" else "option",
                optionTextFl = "n",
                optionName = if (firstSku?.color != null && firstSku.size != null) {
                    "색상^|^사이즈"
                } else if (firstSku?.color != null) {
                    "색상"
                } else if (firstSku?.size != null) {
                    "사이즈"
                } else {
                    ""
                },
                optionData = if (product.sku.isNotEmpty()) {
                    product.sku.mapIndexed { index, sku ->
                        OptionData(
                            idx = index + 1,
                            optionNo = index + 1,
                            optionValue1 = sku.color?.name,
                            optionValue2 = sku.size?.name,
                            optionPrice = if (sku.price.to != null) sku.price.to - sku.price.from else BigDecimal.ZERO
                        )
                    }
                } else {
                    null
                },
                fixedPrice = product.price.from,
                costPrice = product.buyPrice?.from,
                goodsPrice = product.salePrice?.from,
                magnifyImageData = product.images.mapIndexed { index, path ->
                    ImageData(idx = index + 1, text = "$imageDomain$path")
                },
                detailImageData = product.images.mapIndexed { index, path ->
                    ImageData(idx = index + 1, text = "$imageDomain$path")
                },
                // 상품 이미지의 경우 확대 이미지, 상세 이미지를 제외한 나머지 이미지의 경우 한장씩만 등록해주셔야합니다.
                listImageData = product.images.mapIndexed { index, path ->
                    ImageData(idx = index + 1, text = "$imageDomain$path")
                }.take(1),
                mainImageData = product.images.mapIndexed { index, path ->
                    ImageData(idx = index + 1, text = "$imageDomain$path")
                }.take(1),
                goodsMustInfoData = listOf(
                    GoodsMustInfoData(
                        idx = 1,
                        stepData = listOf(
                            StepData(infoTitle = "test", infoValue = "test value")
                        )
                    )
                ),
                goodsDescription = product.description?.toHtml(),
                goodsModelNo = product.shopStyleNumber
            )
        }
    }
}

@JacksonXmlRootElement(localName = "data")
data class GodoUpdateProduct(
    val goods_data: GoodsData,
    @JacksonXmlElementWrapper(namespace = "goods_data", useWrapping = true)
    val goodsNo: String
)

@JacksonXmlRootElement(localName = "data")
data class GodoProduct(
    val goods_data: GoodsData? = null
) {
    companion object {
        fun from(product: Product): GodoProduct {
            return GodoProduct(
                goods_data = GoodsData.from(product)
            )
        }
    }
}