package com.vicc.scrap.application

import com.vicc.scrap.domain.*
import com.vicc.scrap.logger
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.random.Random

interface ProductTranslateService {
    fun translate(product: Product): Product
    fun translateB2c(product: Product): Product
}

@Service
class PapagoProductTranslateService(
    private val translateService: TranslateService,
    private val currencyService: CurrencyService,
) : ProductTranslateService {
    private val categoryMap = mapOf<String, Category>(
        "20000035" to Category(fullName = listOf("여성의류", "상의", "티셔츠"), fullId = listOf("001", "001", "001")),
        "20000018" to Category(fullName = listOf("여성의류", "상의", "셔츠/남방"), fullId = listOf("001", "001", "002")),
        "20000019" to Category(fullName = listOf("여성의류", "상의", "블라우스"), fullId = listOf("001", "001", "003")),
        "20000068" to Category(fullName = listOf("여성의류", "상의", "니트/스웨터"), fullId = listOf("001", "001", "005")),
        "20000017" to Category(fullName = listOf("여성의류", "상의", "니트/스웨터"), fullId = listOf("001", "001", "005")),
        "20000038" to Category(fullName = listOf("여성의류", "상의", "니트/스웨터"), fullId = listOf("001", "001", "005")),
        "20000037" to Category(fullName = listOf("여성의류", "아우터", "슈트/정장"), fullId = listOf("001", "002", "009")),
        "20000389" to Category(fullName = listOf("여성의류", "아우터", "슈트/정장"), fullId = listOf("001", "002", "009")),
        "20000025" to Category(fullName = listOf("여성의류", "아우터", "슈트/정장"), fullId = listOf("001", "002", "009")),
        "20000024" to Category(fullName = listOf("여성의류", "아우터", "슈트/정장"), fullId = listOf("001", "002", "009")),
        "20000129" to Category(fullName = listOf("여성의류", "아우터", "자켓/점퍼"), fullId = listOf("001", "002", "001")),
        "20000128" to Category(fullName = listOf("여성의류", "아우터", "자켓/점퍼"), fullId = listOf("001", "002", "001")),
        "20000174" to Category(fullName = listOf("여성의류", "아우터", "코트"), fullId = listOf("001", "002", "007")),
        "20000070" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000069" to Category(fullName = listOf("여성의류", "아우터", "자켓/점퍼"), fullId = listOf("001", "002", "001")),
        "20000175" to Category(fullName = listOf("여성의류", "아우터", "조끼"), fullId = listOf("001", "002", "008")),
        "20000071" to Category(fullName = listOf("여성의류", "아우터", "자켓/점퍼"), fullId = listOf("001", "002", "001")),
        "20000176" to Category(fullName = listOf("여성의류", "아우터", "조끼"), fullId = listOf("001", "002", "008")),
        "20000067" to Category(fullName = listOf("여성의류", "아우터", "슈트/정장"), fullId = listOf("001", "002", "009")),
        "20000073" to Category(fullName = listOf("여성의류", "아우터", "가죽/퍼"), fullId = listOf("001", "002", "010")),
        "20000074" to Category(fullName = listOf("여성의류", "아우터", "가죽/퍼"), fullId = listOf("001", "002", "010")),
        "20000364" to Category(fullName = listOf("여성의류", "상의", "민소매/나시"), fullId = listOf("001", "001", "007")),
        "20000370" to Category(fullName = listOf("여성의류", "상의", "민소매/나시"), fullId = listOf("001", "001", "007")),
        "20000055" to Category(fullName = listOf("여성의류", "원피스/드레스", "원피스"), fullId = listOf("001", "003", "001")),
        "20000028" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000052" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000072" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000027" to Category(fullName = listOf("여성의류", "원피스/드레스", "원피스"), fullId = listOf("001", "003", "001")),
        "20000131" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000005" to Category(fullName = listOf("여성의류", "아우터", "조끼"), fullId = listOf("001", "002", "008")),
        "20000012" to Category(fullName = listOf("여성의류", "상의", "니트/스웨터"), fullId = listOf("001", "001", "005")),
        "20000007" to Category(fullName = listOf("여성의류", "상의", "티셔츠"), fullId = listOf("001", "001", "001")),
        "20000013" to Category(fullName = listOf("여성의류", "아우터", "슈트/정장"), fullId = listOf("001", "002", "009")),
        "20000008" to Category(fullName = listOf("여성의류", "상의", "셔츠/남방"), fullId = listOf("001", "001", "002")),
        "20000011" to Category(fullName = listOf("여성의류", "상의", "니트/스웨터"), fullId = listOf("001", "001", "005")),
        "20000002" to Category(fullName = listOf("여성의류", "상의", "블라우스"), fullId = listOf("001", "001", "003")),
        "20000014" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000039" to Category(fullName = listOf("여성의류", "상의", "블라우스"), fullId = listOf("001", "001", "003")),
        "20000042" to Category(fullName = listOf("여성의류", "상의", "티셔츠"), fullId = listOf("001", "001", "001")),
        "20000046" to Category(fullName = listOf("여성의류", "아우터", "조끼"), fullId = listOf("001", "002", "008")),
        "20000048" to Category(fullName = listOf("여성의류", "아우터", "슈트/정장"), fullId = listOf("001", "002", "009")),
        "20000106" to Category(fullName = listOf("여성의류", "원피스/드레스", "원피스"), fullId = listOf("001", "003", "001")),
        "20000006" to Category(fullName = listOf("여성의류", "원피스/드레스", "원피스"), fullId = listOf("001", "003", "001")),
        "20000041" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000001" to Category(fullName = listOf("여성의류", "하의", "스커트"), fullId = listOf("001", "005", "001")),
        "20000000" to Category(fullName = listOf("여성의류", "하의", "스커트"), fullId = listOf("001", "005", "001")),
        "20000010" to Category(fullName = listOf("여성의류", "하의", "스커트"), fullId = listOf("001", "005", "001")),
        "20000044" to Category(fullName = listOf("여성의류", "하의", "스커트"), fullId = listOf("001", "005", "001")),
        "20000036" to Category(fullName = listOf("여성의류", "하의", "스커트"), fullId = listOf("001", "005", "001")),
        "20000022" to Category(fullName = listOf("여성의류", "하의", "스커트"), fullId = listOf("001", "005", "001")),
        "20000054" to Category(fullName = listOf("여성의류", "하의", "스커트"), fullId = listOf("001", "005", "001")),
        "20000026" to Category(fullName = listOf("여성의류", "원피스/드레스", "원피스"), fullId = listOf("001", "003", "001")),
        "20000021" to Category(fullName = listOf("여성의류", "하의", "청바지"), fullId = listOf("001", "005", "004")),
        "20000020" to Category(fullName = listOf("여성의류", "하의", "팬츠"), fullId = listOf("001", "005", "005")),
        "20000057" to Category(fullName = listOf("여성의류", "하의", "레깅스"), fullId = listOf("001", "005", "006")),
        "20000009" to Category(fullName = listOf("여성의류", "하의", "팬츠"), fullId = listOf("001", "005", "005")),
        "20000291" to Category(fullName = listOf("여성의류", "하의", "슬랙스"), fullId = listOf("001", "005", "007")),
        "20000053" to Category(fullName = listOf("여성의류", "하의", "팬츠"), fullId = listOf("001", "005", "005")),
        "20000023" to Category(fullName = listOf("여성의류", "하의", "슬랙스"), fullId = listOf("001", "005", "007")),
        "20000341" to Category(fullName = listOf("여성의류", "하의", "팬츠"), fullId = listOf("001", "005", "005")),
        "20000040" to Category(fullName = listOf("여성의류", "세트", "기타 세트"), fullId = listOf("001", "004", "006")),
        "20000340" to Category(fullName = listOf("여성의류", "하의", "팬츠"), fullId = listOf("001", "005", "005")),

//        "20000053" to Category(fullName = listOf("여성의류", "교복/전통의상", "중국식,전통 의상 청바지"), fullId = listOf("001", "008", "005")),
//
//        "20000038" to Category(fullName = listOf("여성의류","상의","울니트"), fullId = listOf("001","001","006")),
//
//        "20000069" to Category(fullName = listOf("여성의류","아우터","다운패딩"), fullId = listOf("001","002","005")),
//        "20000175" to Category(fullName = listOf("여성의류","아우터","다운패딩조끼"), fullId = listOf("001","002","006")),
//
//        "20000027" to Category(fullName = listOf("여성의류","원피스/드레스","예복,드레스"), fullId = listOf("001","003","002")),
//        "20000026" to Category(fullName = listOf("여성의류","원피스/드레스","웨딩드레스"), fullId = listOf("001","003","003")),
//
//        "20000037" to Category(fullName = listOf("여성의류","세트","울니트세트"), fullId = listOf("001","004","003")),
//
//        "20000340" to Category(fullName = listOf("여성의류","하의","겨울바지"), fullId = listOf("001","005","009")),
//
//        "20000039" to Category(fullName = listOf("여성의류","중장년","레이스,쉬폰셔츠/블라우스"), fullId = listOf("001","006","001")),
//        "20000046" to Category(fullName = listOf("여성의류","중장년","아우터/조끼"), fullId = listOf("001","006","003")),
//        "20000048" to Category(fullName = listOf("여성의류","중장년","세트"), fullId = listOf("001","006","004")),
//        "20000044" to Category(fullName = listOf("여성의류","중장년","스커트"), fullId = listOf("001","006","006")),
//        "20000040" to Category(fullName = listOf("여성의류","중장년","하의"), fullId = listOf("001","006","007")),
//
//        "20000005" to Category(fullName = listOf("여성의류","빅사이즈","아우터/조끼"), fullId = listOf("001","007","001")),
//        "20000012" to Category(fullName = listOf("여성의류","빅사이즈","울니트"), fullId = listOf("001","007","002")),
//        "20000008" to Category(fullName = listOf("여성의류","빅사이즈","셔츠"), fullId = listOf("001","007","004")),
//        "20000011" to Category(fullName = listOf("여성의류","빅사이즈","맨투맨/후드"), fullId = listOf("001","007","005")),
//        "20000002" to Category(fullName = listOf("여성의류","빅사이즈","레이스,쉬폰셔츠/블라우스"), fullId = listOf("001","007","006")),
//        "20000013" to Category(fullName = listOf("여성의류","빅사이즈","세트"), fullId = listOf("001","007","010")),
//        "20000072" to Category(fullName = listOf("여성의류","빅사이즈","기타"), fullId = listOf("001","007","011")),
//
//        "20000072" to Category(fullName = listOf("여성의류","교복/전통의상","학생교복"), fullId = listOf("001","008","001")),
//        "20000055" to Category(fullName = listOf("여성의류","교복/전통의상","치파오"), fullId = listOf("001","008","002")),
//        "20000052" to Category(fullName = listOf("여성의류","교복/전통의상","중국식,전통의상"), fullId = listOf("001","008","003")),
//        "20000054" to Category(fullName = listOf("여성의류","교복/전통의상","중국식,전통의상스커트"), fullId = listOf("001","008","004")),
//        "20000028" to Category(fullName = listOf("여성의류","교복/전통의상","민족(중국전통)의상/무대의상"), fullId = listOf("001","008","006")),
//        "20000131" to Category(fullName = listOf("여성의류","교복/전통의상","호텔작업복"), fullId = listOf("001","008","007")),
    )

    override fun translate(product: Product): Product {
        return try {
            product.copy(
                name = translateService.translate(product.name),
                colors = translateOption(product.colors)
            )
        }catch (e: Exception) {
            logger().error("translate Exception: $product")
            e.printStackTrace()
            product
        }
    }

    fun price(price: Price, magnification: Double = 3.0): Price {
        fun getPrice(orgPrice: BigDecimal, magnification: Double): BigDecimal {
            return (currencyService.cny() * orgPrice * BigDecimal(magnification)).setScale(-2, BigDecimal.ROUND_UP)
//            return (currencyService.cny() * orgPrice * BigDecimal(magnification)).setScale(-2, BigDecimal.ROUND_UP)
        }

        return Price(
            from = getPrice(price.from, magnification),
            to = getPrice(price.to ?: BigDecimal.valueOf(0), magnification)
        )
    }

    fun category(product: Product): Category {
        if (categoryMap.containsKey(product.categoryCn!!.fullId.last())) {
            return categoryMap[product.categoryCn!!.fullId.last()]!!
        } else {
            return categoryMap["20000070"]!!
//            throw IllegalArgumentException("category not mapped: ${product.categoryCn!!.fullId}, product: ${product.toString()}")
        }
    }

    private fun translateOption(options: List<Option>): List<Option> {
        return options.map { Option(name = translateService.translate(it.name), imageLink = it.imageLink) }
    }

    private fun changeSizeName(option: Option): Option {
        val optionWordMap = mapOf(
            "F" to "Free (55~66)",
            "Free" to "Free (55~66)",
            "XS" to "XS (44)",
            "S" to "S (55)",
            "M" to "M (66)",
            "L" to "L (77)",
            "XL" to "XL (88)"
        )
        return if (optionWordMap.containsKey(option.name)) {
            Option(name = optionWordMap[option.name]!!, imageLink = option.imageLink)
        } else {
            option
        }
    }

    override fun translateB2c(product: Product): Product {
        return product.copy(
            categoryKr = category(product),
            name = translateService.forbiddenWord(product.name),
            colors = translateOption(product.colors),
            sizes = translateOption(product.sizes),
            attribute = product.attribute.map {
                Attribute(
                    key = translateService.translate(it.key),
                    value = translateService.translate(it.value)
                )
            },

            price = price(product.price, Random.nextDouble(4.5, 5.5)),
            salePrice = price(product.price, 3.0),
            buyPrice = price(product.price, 1.4),
            sku = if (product.sku.isEmpty()) {
                listOf(
                    Sku(
                        color = Option(name = "단일색상"),
                        size = Option(name = "Free (55-66)"),
                        price = price(Price(from = BigDecimal(0), to = BigDecimal(0))),
                        orderAble = true
                    )
                )
            } else {
                product.sku.mapIndexed { index, sku ->
                    Sku(
                        color = product.sku[index].color?.let { translateOption(listOf(it)).first() },
                        size = product.sku[index].size?.let { changeSizeName(translateOption(listOf(it)).first()) },
                        price = price(Price(from = product.price.from, to = product.sku[index].price.from)),
                        orderAble = product.sku[index].orderAble
                    )
                }
            },
            description = product.description?.let { description ->
                Description(rows = description.rows.map {
                        if(it.rowType == DescriptionRow.RowTypes.Image) {
                            it
                        }else {
                            DescriptionRow(DescriptionRow.RowTypes.String, translateService.translate(it.getRow()))
                        }
                    }.toMutableList()
                )
            }
        )
    }
}