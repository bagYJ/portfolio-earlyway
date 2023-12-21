package com.vicc.scrap.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal

val GL_PRODUCT = Product(
    id = "test",
    name = "test product",
    images = listOf("https://img1.vvic.com/upload/1664412110173_59512.jpg_400x400.jpg", "https://img1.vvic.com/upload/1664412110267_549094.jpg_400x400.jpg"),
    colors = listOf(
        Option(name = "검정", imageLink = "https://img1.vvic.com/upload/1664412110173_59512.jpg_400x400.jpg"),
        Option(name = "파랑", imageLink = "https://img1.vvic.com/upload/1664412110173_59512.jpg_400x400.jpg"),
    ),
    shopStyleNumber = "tttt",
    sizes = listOf(
        Option(name = "55"),
        Option(name = "44"),
        Option(name = "33"),
    ),
    linkUrl = "some link",
    shop = Shop(
        address = "우리집",
        phoneNumber = "010-4055-7196",
        name = "잘팔리는 집",
        popularity = "1",
        messengerId = "line",
        origin = "기흥구",
        id = "2345"
    ),
    price = Price(from = BigDecimal.valueOf(33)),
    productMeta = ProductMeta(
        releaseDate = "2022-11-01",
        saleCount = 400,
        uploadCount = 3,
        wishCount = 33
    ),
    categoryCn = Category(
        fullId = listOf("234512"),
        fullName = listOf("대", "중", "소", "세")
    ),
    attribute = listOf(
        Attribute(key = "색깔은", "이쁘구요"),
        Attribute(key = "재질도", "좋아요"),
    ),
    description = Description(
        rows = listOf(
            "https://img1.vvic.com/upload/1664412110241_74532.jpg_400x400.jpg",
            "https://img1.vvic.com/upload/1664412110265_566363.png_400x400.jpg"
        ).map { DescriptionRow(rowType = DescriptionRow.RowTypes.Image, row = it) }.toMutableList()
    )
)

class ProductTest:FunSpec({
    test("동일 객체 체크") {
        GL_PRODUCT shouldBe GL_PRODUCT
        GL_PRODUCT shouldBe GL_PRODUCT.copy()
        GL_PRODUCT shouldNotBe  GL_PRODUCT.copy(name="다른 이름")
        GL_PRODUCT shouldNotBe  GL_PRODUCT.copy(attribute= listOf(Attribute(key = "색깔은", "이쁘구요"),
            Attribute(key = "재질도", "좋아요?")))

        GL_PRODUCT shouldBe  GL_PRODUCT.copy(attribute= listOf(Attribute(key = "색깔은", "이쁘구요"),
            Attribute(key = "재질도", "좋아요")))
    }
})