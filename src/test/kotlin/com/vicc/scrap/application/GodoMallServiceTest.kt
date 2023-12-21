package com.vicc.scrap.application

import com.vicc.scrap.domain.GL_PRODUCT
import com.vicc.scrap.logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.apache.commons.net.ftp.FTPClient
import org.springframework.core.io.DefaultResourceLoader
import javax.imageio.ImageIO

class GodoMallServiceTest : FunSpec({


 val service = GodoMallService()
    test("ftp test") {
        val client = FTPClient()
        client.connect("121.143.79.109", 21)
        val isLogin = client.login("mmi12by_godo", "u9A_rl3kvgXlFPU")
        logger().info("isLogin = $isLogin")
        val list = client.listFiles("/")
        logger().info("1234 = $list")

//        client.setFileType(FTP.ASCII_FILE_TYPE)
        client.mkd("/public_html/goods/images/test")
        client.storeFile("/public_html/goods/images/test/test.png", DefaultResourceLoader().getResource("classpath:images/1662048800726_127781.jpeg").inputStream)
        client.logout()
    }

    test("uploadDetailImage") {
        val product = GL_PRODUCT.copy(
            id = "63de4bef5243f400081ababa324",
            images = listOf(
            "https://img.alicdn.com/bao/uploaded/i2/383761164/O1CN01vLn00R1KT9l97tK6T_!!383761164.jpg_400x400.jpg",
            "https://img.alicdn.com/bao/uploaded/i1/383761164/O1CN01aRdBr21KT9kyTIkCi_!!383761164.jpg_400x400.jpg",
            "https://img.alicdn.com/bao/uploaded/i3/383761164/O1CN01M2a47z1KT9lAx9ihZ_!!383761164.jpg_400x400.jpg",
            "https://img.alicdn.com/bao/uploaded/i4/383761164/O1CN01rUn3Od1KT9lBit0d8_!!383761164.jpg_400x400.jpg",
            "https://img.alicdn.com/bao/uploaded/i1/383761164/O1CN018BOmI11KT9l6HmRWz_!!383761164.jpg_400x400.jpg"
        ))
        service.uploadDetailImage(product)

    }
    test("save") {
        val product = GL_PRODUCT.copy(
            id = "63de4bef5243f400081ababa444",
            images = listOf(
                "https://img.alicdn.com/bao/uploaded/i2/383761164/O1CN01vLn00R1KT9l97tK6T_!!383761164.jpg_400x400.jpg",
                "https://img.alicdn.com/bao/uploaded/i1/383761164/O1CN01aRdBr21KT9kyTIkCi_!!383761164.jpg_400x400.jpg",
                "https://img.alicdn.com/bao/uploaded/i3/383761164/O1CN01M2a47z1KT9lAx9ihZ_!!383761164.jpg_400x400.jpg",
                "https://img.alicdn.com/bao/uploaded/i4/383761164/O1CN01rUn3Od1KT9lBit0d8_!!383761164.jpg_400x400.jpg",
                "https://img.alicdn.com/bao/uploaded/i1/383761164/O1CN018BOmI11KT9l6HmRWz_!!383761164.jpg_400x400.jpg"
            ))
        service.save(product)
    }
    test("resize image") {
        service.resize(DefaultResourceLoader().getResource("classpath:images/1662048800726_127781.jpeg").file.toPath())
        val resized = ImageIO.read(DefaultResourceLoader().getResource("classpath:images/1662048800726_127781.jpeg").file)
        resized.width shouldBe resized.height
    }


    test("image 깨짐") {
        val scrapService = JsoupScrapServiceImpl()
        scrapService.profile = "local"

        val product = scrapService.product("https://www.vvic.com/item/6469f2e8a4a8660008ec5455")
        service.uploadProductImage(product)

    }
})
