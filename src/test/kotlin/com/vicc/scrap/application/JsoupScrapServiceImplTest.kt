package com.vicc.scrap.application

import com.vicc.scrap.logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.bigdecimal.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal

class JsoupScrapServiceImplTest : FunSpec({

    val service = JsoupScrapServiceImpl()
    service.profile = "local"

    test("product") {
        val product = service.product("https://www.vvic.com/item/63482919a919730006a34588")
        logger().info(product.toString())
    }

    test("product single size") {
        val product = service.product("https://www.vvic.com/item/626c1d18c7b11a00069ce89e")

        product.id shouldBe "626c1d18c7b11a00069ce89e"
        product.name shouldBe "现货 泡泡袖V领宽松连衣裙"
        product.price.from shouldBe "55.00".toBigDecimal()
        product.price.to shouldBe null
        product.colors[0].name shouldBe "粉红色"
        product.colors[1].name shouldBe "卡其色"
        product.colors[2].name shouldBe "蓝色"
        product.colors[3].name shouldBe "藏青色"
        product.shop.phoneNumber shouldBe "17788710316"
        product.shop.address shouldBe "国泰 4楼 4036-A"
        product.sizes[0].name shouldBe "均码"
        product.shopStyleNumber shouldBe "1459#"
    }

    // 20220928 버전 추가
    test("shopStyleNumber 다른 layout") {
        val product = service.product("https://www.vvic.com/item/630b422397d30c000643549b")
        product.shopStyleNumber shouldBe "7560档口现货#"
    }

    // 20220928 버전 추가
    test("gold merchant") {
        val product = service.product("https://www.vvic.com/item/62e4c12e54ee38000690a278")
        product.shop.address shouldBe "国大 7楼 747-B"
    }

    // 20220928 버전 추가
    test("순위 없는 merchant") {
        val product = service.product("https://www.vvic.com/item/6318f7e9096bdd00064db905")
        product.shop.address shouldBe "国泰 4楼 4107-A"
        product.shop.phoneNumber shouldBe "13265379386"
    }

    // 20220928 버전 추가
    test("旺旺 있는 merchant") {
        val product = service.product("https://www.vvic.com/item/62e4c12e54ee38000690a278")
        product.shop.address shouldBe "国大 7楼 747-B"
    }

    test("product multi size") {
        val product = service.product("https://www.vvic.com/item/63201477096bdd000651681e")
        product.sizes[0].name shouldBe "M"
        product.sizes[1].name shouldBe "L"
        product.sizes[2].name shouldBe "XL"
    }

    test("product multi price") {
        val product = service.product("https://www.vvic.com/item/62769ec79e8510000669f070")
        product.price.from shouldNotBe null
        product.price.to shouldNotBe null
    }

    test("2022-10-13 색상 수집 못하는 상품") {
        val product = service.product("https://www.vvic.com/item/633d3a7239f6de00063cbbae")
        product.colors.forEach {
            it.name.isEmpty() shouldNotBe false
        }
    }

//    test("productIdsFromCategory") {
//        val ids = service.productIdList("20000106", 1)
//        ids.isEmpty() shouldBe false
//    }

    test("added property") {// B2C 작업하면서 늘어난 프로퍼티들
        val product = service.product("https://www.vvic.com/item/6334ea5cf963b300060a8c13")

        //이미지
        product.images[0] shouldBe "https://img1.vvic.com/upload/1664412110173_59512.jpg"
        product.images[1] shouldBe "https://img1.vvic.com/upload/1664412110267_549094.jpg"
        product.images[2] shouldBe "https://img1.vvic.com/upload/1664412110241_74532.jpg"
        product.images[3] shouldBe "https://img1.vvic.com/upload/1664412110265_566363.png"
        product.images[4] shouldBe "https://img1.vvic.com/upload/1664412110344_985647.png"


        product.shop.name shouldBe "婷婷大码 - 每日上新"
        product.shop.messengerId shouldBe "TT028028028028"
        product.shop.origin shouldBe "广东省 广州"
        product.shop.popularity shouldBe "139"

        product.productMeta.releaseDate shouldBe "2022-09-29 08:38:48"
        product.productMeta.saleCount shouldBe 1L
        product.productMeta.uploadCount shouldBe 40L
        product.productMeta.wishCount shouldBe 34L

        product.attribute.isEmpty() shouldBe false

        product.description!!.rows shouldBe 9
        product.description!!.rows[0].row shouldBe "https://img1.vvic.com/upload/1661667323635_426773.JPG"

        product.categoryCn!!.fullId[0] shouldBe "20000005"
        product.categoryCn!!.fullName.size shouldBe 4
        product.categoryCn!!.fullName[0].isBlank() shouldBe false

    }

    // 몰명 수정, id 추가
    test("몰명 수정, id 추가 2022-11-04") {
        val product1 = service.product("https://www.vvic.com/item/5dab4d3c98474d000119753b")
        val product2 = service.product("https://www.vvic.com/item/634a61f72fd3940008c1ae23")


        product1.shop.name shouldBe "CherryAka卡卡家"
        product1.shop.id shouldBe "18684"

        product2.shop.name shouldBe "【路卡缇】高端大码装"
        product2.shop.id shouldBe "72877"
    }

    test("sku") {
        val product = service.product("https://www.vvic.com/item/62e409a654ee3800069076a5")

        product.sku.size shouldBeGreaterThan 10
        product.sku[0].color!!.name.isNotBlank() shouldBe true
        product.sku[0].size!!.name.isNotBlank() shouldBe true
        product.sku[0].price.from shouldBeGreaterThan  BigDecimal.valueOf(0)

        val product_one_option = service.product("https://www.vvic.com/item/6319bbc0b15f6100064dc4c0")
        product_one_option.sku.isEmpty() shouldBe true
    }

    test("addr") {
        val product = service.product("https://www.vvic.com/item/636a775da4a8660008ba2564")
        product.shop.address.isBlank() shouldNotBe true
    }
    test("productIdList") {
        val ids = service.productIdList("20000035", 1, "cu=B2FEDD5776C8CCDFC24E99670C0AE0AD; chash=1633501382; _countlyIp=218.50.57.83; _uab_collina=166246364812495694439023; _ga=GA1.2.1082465848.1662463648; hasFocusStall=false; ISSUPPORTPANGGE=true; hasCityMarket=\"\"; city=gz; SSGUIDE=1; SFEGUIDE=1%2C0%2C0; Hm_lvt_fbb512d824c082a8ddae7951feb7e0e5=1670940502; Hm_lvt_fdffeb50b7ea8a86ab0c9576372b2b8c=1670940502; rankTipShow=1; rankCateogry=15; DEVICE_INFO=%7B%22device_id%22%3A%22B2FEDD5776C8CCDFC24E99670C0AE0AD%22%2C%22device_channel%22%3A1%2C%22device_type%22%3A1%2C%22device_model%22%3A%22Mac%22%2C%22device_os%22%3A%22Mac%20OS%22%2C%22device_lang%22%3A%22ko-KR%22%2C%22device_size%22%3A%221728*1117%22%2C%22device_net%22%3A%220%22%2C%22device_lon%22%3A%22%22%2C%22device_lat%22%3A%22%22%2C%22device_address%22%3A%22%22%2C%22browser_type%22%3A%22Chrome%22%2C%22browser_version%22%3A%22108.0.0.0%22%7D; ipCity=218.50.57.83%2C%E5%B9%BF%E4%B8%9C%20%E5%B9%BF%E5%B7%9E%E5%B8%82; ORDERTYPE=; _HOTVIEWDATE=1673362799000; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%222965120%22%2C%22first_id%22%3A%22183128e2156102b-0f6bf2cbd1159-1b525635-1930176-183128e215714c9%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%9B%B4%E6%8E%A5%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC_%E7%9B%B4%E6%8E%A5%E6%89%93%E5%BC%80%22%2C%22%24latest_referrer%22%3A%22%22%2C%22page_attr%22%3A%22hot%22%7D%2C%22%24device_id%22%3A%22183128e2156102b-0f6bf2cbd1159-1b525635-1930176-183128e215714c9%22%7D; Hm_lpvt_fbb512d824c082a8ddae7951feb7e0e5=1673359155; Hm_lpvt_fdffeb50b7ea8a86ab0c9576372b2b8c=1673359155; ssxmod_itna=YqGxnDgDBDcDyADu7DzOD2iXDkC2YqRPwhqbePRKwKDswWDLxlKidDdxYPa=oxahxQYA0rLhAUB7G+HHW/8OTa=wnjFm=KWAh4GIDeKG2DmeDyDi5GRD0IIsD44GTDt4DTD34DYDixtDB/w=cDGPZDWicnuLdnqDADiUOQeGv/T=uUTNqD0pdDYpFAkIODBjxcMQ2BjNe5lT3+GGFmp+s7+4Kzo+dWDxqYYL3DgHQW7GxfAGdQGh=3bhsRi8k1l44RBo=eD=; ssxmod_itna2=YqGxnDgDBDcDyADu7DzOD2iXDkC2YqRPwhqbePRKqikjHqQDl=U1xjbg017uKZ=D6jkuXxYe8G06oR7Yeo4OdHzr2WhBKh1OeYKXhM5ckH26W3gLmwxevnuyCDe=y0V+XtM=ByfTztC4S=MUYiVBqLxXODliCGYAdr9QDdC+7G4o0RoOStMbuTtmmh=Rj3zmO2V9MfaVCEPq1oueSoZfT01z7KDaevxUBy0+QUrwpiRtzXd9Q910aUhv6QGFj=mLIjipc86Kq2Ack2I6TEh=YuBbsBA2KrUxUbD1Gh2=YQql8IPq3+0ITV/HlnIiCi+S55/niGbxq4bK5b8T4wRXfTVzRC+cTj++GI1KHRznQ+qYCK+F9b8mmn777eVQ9N44/KAfAO+nPw8boeRtKaYQ77RniUnPnbeFGLiSV4RKYD0CDxIrSAom74K8KxKDdD07=wx5GK8hxr7op8q4PtaheBAWNODPDNwPql7Py2QLaPgADQ4KkDtwaXEL3Hi6yg4VToBwxQACKZ0o/Dewma2KrKswY4IgkYt5bIqBZteD6pP7bPq+anm/E4pDD7=DYKxeD===; acw_tc=2f624a0c16733695380448256e171fba9ed73014fd299edf0e5d0ba44ab19f; ocity=gz; vvic_token=878d9b83-e0a0-4c18-a979-85e1fbd01645")
        logger().info(ids.joinToString { it })
    }

    test("description translate") {
        val product = service.product("https://www.vvic.com/item/64ea03e65243f40008f32440")

        logger().info(product.toString())
    }

})
