package com.vicc.scrap.application

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vicc.scrap.domain.*
import com.vicc.scrap.logger
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.*
import java.util.*

interface ScrapService {
    fun product(url: String): Product
    fun productById(id: String): Product
    fun productIdsFromCategory(categoryId: String, page: Int): List<String>
}

@Service
class JsoupScrapServiceImpl : ScrapService {

    private lateinit var cookie: MutableMap<String, String>
    private val document: ThreadLocal<Document> = ThreadLocal()
    private val mapper = jacksonObjectMapper()

    @Value("\${spring.profiles.active}") var profile: String = "consumer"

    private val restTemplate = RestTemplateBuilder()
        .rootUri("https://www.vvic.com")
        .setReadTimeout(Duration.ofSeconds(60 * 2))
        .setConnectTimeout(Duration.ofSeconds(60))
        .build()
    private val nodeRestTemplate = RestTemplateBuilder()
        .rootUri("http://124.50.239.169:3000")
        .defaultHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        .build()

    init {
        login()
    }

    private fun login() {
        val connection = Jsoup.connect("https://www.vvic.com/apic/login")
            .timeout(1000*60*3)
            .method(Connection.Method.POST)
            .ignoreContentType(true)
            .headers(
                mapOf(
                    "content-type" to "application/x-www-form-urlencoded; charset=UTF-8",
                )
            )
            .data("username", "ilpa01@naver.com")
            .data("password", "rlfdjwls1!")
            .data("auto", "1")
//            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:106.0) Gecko/20100101 Firefox/106.0")
            .execute()

        cookie = connection.cookies()
        cookie.forEach { t, u -> logger().info("cookie $t = $u") }
    }

    private fun setDocument(url: String): JsonNode {
        val connection = Jsoup.connect(url)
            .timeout(1000*60*3)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:106.0) Gecko/20100101 Firefox/106.0")
            .cookies(cookie)
            .ignoreContentType(true)

        if(profile == "consumer") {
            connection.proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 9050)))
        }
//        document.set(connection.get())
        return ObjectMapper().readTree(connection.execute().body())["data"]
    }

    private fun singleNodeValue(path: String): String {
        return document.get().select(path).text().trim()
    }

    private fun multiNodes(path: String): Elements {
        return document.get().select(path)
    }

    @Retryable(maxAttempts = 3)
    override fun product(url: String): Product {
        logger().info("url => $url")
        val json = setDocument("https://www.vvic.com/apif/item/${url.split("/").last()}/detail")
        val shop = setDocument("https://www.vvic.com/apif/shop/profile?vid=${url.split("/").last()}&entityType=1")["shopInfo"]
        val price = json.get("price").asText()
        return Product(
            id = url.split("/").last(),
            name = json["title"].asText(),
            price = price(price),
            images = json["imgs"].asText().split(","),
            colors = colors(json["color"].asText().split(","), ObjectMapper().readTree(json["color_pics"].asText()).map { it.asText() }),
            linkUrl = url,
            shop = shop(json, shop),
            shopStyleNumber = json["attrs_json"]["货号"].asText(),
            sizes = json["size"].asText().split(",").map { Option(name = it) },
            productMeta = ProductMeta(
                releaseDate = json["up_time"].asText().split(" ")[0],
                saleCount = json["sales"]?.asText()?.toLong(),
                uploadCount = json["cur_upload_num"]?.asText()?.toLong(),
                wishCount = json["cur_fav_item_num"]?.asText()?.toLong()
            ),
            attribute = json["attrs"].asText().split(",").map { Attribute(it.split(":")[0], it.split(":")[1]) },
            description = description(json["item_desc"]["desc"].asText()),
            categoryCn = Category(
                fullId = json["vcid"].asText().split(","),
                fullName = ObjectMapper().readTree(json["breadCrumbs"].asText()).map { it.get("name").asText() }
            ),
            sku = sku(url.split("/").last(), json["skumap"].asText()),
            buyPrice = price(price)

        )
    }

    private fun sku(key: String, skuJsonString: String): List<Sku> {
//        val skuJsonString = stringFromScript("var _SKUMAP = ")
        if(skuJsonString.isNullOrBlank())return emptyList()
        val skuJson = nodeRestTemplate.postForObject("/decode", "key=item_detail_$key&text=$skuJsonString", String::class.java)
        logger().info("option value ${skuJson}")
        return try {
            val tree = mapper.readTree(skuJson)
            if(tree.size() < 2) return emptyList()
            tree.map {
                val status = it["status"].asInt()
                val skuState = it["sku_state"].asInt()
                val isLack = it["is_lack"].asInt()
                Sku(
                    color = Option(name = it["color_name"].asText()),
                    size = Option(name = it["size_name"].asText()),
                    price = Price(from = BigDecimal.valueOf(it["discount_price"].asDouble())),
                    orderAble = isLack != 0 && status == 1 && skuState == 3
                )
            }
        }catch (e: Exception) {
            println(e.message)
            emptyList()
        }
    }

    private fun category(): Category {
        val categoryNames = multiNodes("body > div.deatil-wrapper > nav > a")
        return Category(
            fullId = fromScript("var _VCID = "),
            fullName = categoryNames.map { it.text().trim() }
        )
    }

    private fun description(images: String): Description {
//        val images = document.get().select("#descTemplate").html()
        val description = Description(mutableListOf())

        Jsoup.parse(images).body().children().forEach {
            if(it.`is`("img")) {
                description.push(DescriptionRow.RowTypes.Image, it.attr("src"))
                logger().info(it.toString())
            }
            if(it.text().trim().isNotBlank()) {
                description.push(DescriptionRow.RowTypes.String, it.text())
                logger().info(it.text())
            }
            if (it.select("img").isNotEmpty()) {
                it.select("img").forEach { img ->
                    description.push(DescriptionRow.RowTypes.Image, img.attr("src"))
                    logger().info(img.toString())
                }
            }
        }
        return description
    }

    override fun productById(id: String): Product {
        return this.product("https://www.vvic.com/item/$id")
    }

    @Retryable(maxAttempts = 5)
    override fun productIdsFromCategory(categoryId: String, page: Int): List<String> {
        return productIdList(categoryId, page, categoryScrapCookie)
    }
    val categoryScrapCookie = "cu=B2FEDD5776C8CCDFC24E99670C0AE0AD; chash=1633501382; _countlyIp=218.50.57.83; _uab_collina=166246364812495694439023; _ga=GA1.2.1082465848.1662463648; hasFocusStall=false; ISSUPPORTPANGGE=true; hasCityMarket=\"\"; city=gz; SSGUIDE=1; SFEGUIDE=1%2C0%2C0; Hm_lvt_fbb512d824c082a8ddae7951feb7e0e5=1670940502; Hm_lvt_fdffeb50b7ea8a86ab0c9576372b2b8c=1670940502; rankTipShow=1; rankCateogry=15; DEVICE_INFO=%7B%22device_id%22%3A%22B2FEDD5776C8CCDFC24E99670C0AE0AD%22%2C%22device_channel%22%3A1%2C%22device_type%22%3A1%2C%22device_model%22%3A%22Mac%22%2C%22device_os%22%3A%22Mac%20OS%22%2C%22device_lang%22%3A%22ko-KR%22%2C%22device_size%22%3A%221728*1117%22%2C%22device_net%22%3A%220%22%2C%22device_lon%22%3A%22%22%2C%22device_lat%22%3A%22%22%2C%22device_address%22%3A%22%22%2C%22browser_type%22%3A%22Chrome%22%2C%22browser_version%22%3A%22108.0.0.0%22%7D; ipCity=218.50.57.83%2C%E5%B9%BF%E4%B8%9C%20%E5%B9%BF%E5%B7%9E%E5%B8%82; ORDERTYPE=; _HOTVIEWDATE=1673362799000; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%222965120%22%2C%22first_id%22%3A%22183128e2156102b-0f6bf2cbd1159-1b525635-1930176-183128e215714c9%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%9B%B4%E6%8E%A5%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC_%E7%9B%B4%E6%8E%A5%E6%89%93%E5%BC%80%22%2C%22%24latest_referrer%22%3A%22%22%2C%22page_attr%22%3A%22hot%22%7D%2C%22%24device_id%22%3A%22183128e2156102b-0f6bf2cbd1159-1b525635-1930176-183128e215714c9%22%7D; Hm_lpvt_fbb512d824c082a8ddae7951feb7e0e5=1673359155; Hm_lpvt_fdffeb50b7ea8a86ab0c9576372b2b8c=1673359155; ssxmod_itna=YqGxnDgDBDcDyADu7DzOD2iXDkC2YqRPwhqbePRKwKDswWDLxlKidDdxYPa=oxahxQYA0rLhAUB7G+HHW/8OTa=wnjFm=KWAh4GIDeKG2DmeDyDi5GRD0IIsD44GTDt4DTD34DYDixtDB/w=cDGPZDWicnuLdnqDADiUOQeGv/T=uUTNqD0pdDYpFAkIODBjxcMQ2BjNe5lT3+GGFmp+s7+4Kzo+dWDxqYYL3DgHQW7GxfAGdQGh=3bhsRi8k1l44RBo=eD=; ssxmod_itna2=YqGxnDgDBDcDyADu7DzOD2iXDkC2YqRPwhqbePRKqikjHqQDl=U1xjbg017uKZ=D6jkuXxYe8G06oR7Yeo4OdHzr2WhBKh1OeYKXhM5ckH26W3gLmwxevnuyCDe=y0V+XtM=ByfTztC4S=MUYiVBqLxXODliCGYAdr9QDdC+7G4o0RoOStMbuTtmmh=Rj3zmO2V9MfaVCEPq1oueSoZfT01z7KDaevxUBy0+QUrwpiRtzXd9Q910aUhv6QGFj=mLIjipc86Kq2Ack2I6TEh=YuBbsBA2KrUxUbD1Gh2=YQql8IPq3+0ITV/HlnIiCi+S55/niGbxq4bK5b8T4wRXfTVzRC+cTj++GI1KHRznQ+qYCK+F9b8mmn777eVQ9N44/KAfAO+nPw8boeRtKaYQ77RniUnPnbeFGLiSV4RKYD0CDxIrSAom74K8KxKDdD07=wx5GK8hxr7op8q4PtaheBAWNODPDNwPql7Py2QLaPgADQ4KkDtwaXEL3Hi6yg4VToBwxQACKZ0o/Dewma2KrKswY4IgkYt5bIqBZteD6pP7bPq+anm/E4pDD7=DYKxeD===; acw_tc=2f624a0c16733695380448256e171fba9ed73014fd299edf0e5d0ba44ab19f; ocity=gz; vvic_token=878d9b83-e0a0-4c18-a979-85e1fbd01645"

    private fun attribute(): List<Attribute> {
        return multiNodes("#info > ul").map {
            it.select("li").text()
        }.filter { !it.isNullOrBlank() && it.contains(":") }
            .map { Attribute(it.split(":")[0], it.split(":")[1]) }
    }

    private fun meta(): ProductMeta {
        return ProductMeta(
            releaseDate = releaseDate().trim(),
            saleCount = metas("销量")?.toLong(),
            uploadCount = metas("上款量")?.toLong(),
            wishCount = metas("收藏量")?.toLong()
        )
    }

    fun productIdList(categoryId: String, page: Int, cookies: String): List<String> {
        val url = "/apif/search/asy?merge=1&algo=0&pid=1&tagid=100&vcid=$categoryId&searchCity=gz&pageId=list_index&currentPage=$page"
        val header = HttpHeaders()
        header.set("Cookie", cookies)
        val request = HttpEntity("", header)
        val response = restTemplate.exchange(url,HttpMethod.GET,  request, Map::class.java)
        return (((response.body!!.get("data")as Map<String, Any>)["search_page"] as Map<String, Any>)["recordList"]as List<Map<String, Any>>).map { it["vid"] as String }
    }

    private fun images(): List<String> {

        val imageWrapper = multiNodes("#thumblist > div")
        return imageWrapper
            .map {
                val imageTag = it.select("div > a > img")
                if (!imageTag.attr("mid").isNullOrBlank()) {
                    imageTag.attr("mid")
                } else if (!imageTag.attr("mid").isNullOrBlank()) {
                    imageTag.attr("big")
                } else {
                    imageTag.attr("src")
                }
            }.filter { !it.isNullOrBlank() }
            .map { it.split("?").firstOrNull() }
            .mapNotNull { it }
            .map { it.replace("_400x400.jpg", "") }
            .map {
                if (it.startsWith("https")) {
                    it
                } else {
                    "https:$it"
                }
            }
    }

    private fun shopStyleNumber(json: JsonNode): String {
        val dls = multiNodes("body > div.deatil-wrapper > article > main > section:nth-child(1) > div.detail.product-detail > div.detail-info > div:nth-child(1) > dl")
        var shopStyleDlIndex = -1
        dls.forEachIndexed { index, element ->
            if (element.select("dt").text().contains("货号")) {
                shopStyleDlIndex = index
            }
        }
        return if (shopStyleDlIndex < 0) {
            "테스트되지 않은 layout입니다. 개발자에게 문의주세요."
        } else {
            dls[shopStyleDlIndex].select("dd").text()
        }
    }

    private fun metas(key: String): String? {
        val lis = multiNodes("body > div.deatil-wrapper > article > main > section:nth-child(1) > div.detail.product-detail > div.detail-info > div:nth-child(2) > ul > li")
        val li = lis.firstOrNull { it.text().contains(key) } ?: return null
        return li.text().split("：")[1]
    }

    private fun releaseDate(): String {
        val dls = multiNodes("body > div.deatil-wrapper > article > main > section:nth-child(1) > div.detail.product-detail > div.detail-info > div:nth-child(1) > dl")
        var releaseDateDlIndex = -1
        dls.forEachIndexed { index, element ->
            if (element.select("dt").text().contains("上新时间")) {
                releaseDateDlIndex = index
            }
        }
        return if (releaseDateDlIndex < 0) {
            "테스트되지 않은 layout입니다. 개발자에게 문의주세요."
        } else {
            val dateString = dls[releaseDateDlIndex].select("dd").text().trim()
            if(dateString.contains(" ")) {
                dateString.split(" ")[0]
            }else {
                dateString
            }

        }
    }

    private fun name(): String {
        return singleNodeValue("body > div.deatil-wrapper > article > main > section:nth-child(1) > div.detail.product-detail > div.detail-name > h1")
    }

    private fun price(price: String): Price {
//        val price = singleNodeValue("body > div.deatil-wrapper > article > main > section:nth-child(1) > div.detail.product-detail > div.detail-price > dl:nth-child(1) > dd")
        return if (price.isNotBlank()) {
            val prices = price.split("-")
            Price(
                from = prices[0].toBigDecimal(),
                to = if (prices.size > 1) {
                    prices[1].toBigDecimal()
                } else {
                    null
                }
            )
        } else {
            Price(from = BigDecimal.valueOf(0))
        }
    }
    private fun colors(color: List<String>, colorPic: List<String>): List<Option> {
        val colorImages = colorPic
            .map {
                if(!it.isNullOrBlank()) {
                    if (it.startsWith("https")) {
                        it
                    } else {
                        "https:$it"
                    }
                }else{
                    it
                }
            }

        return color.mapIndexed { index, s ->
            Option(name = s, imageLink = colorImages.getOrNull(index))
        }
    }

    private fun shop(json: JsonNode, shop: JsonNode): Shop {

        return Shop(
            address = shop["address2"].asText().replace("\uE62C", "").trim(),
            phoneNumber = shop["telephone"].asText(),
            name = shop["name"].asText().trim().replace(" 修改 信息", ""),
            popularity = shop["top"].asText(),
            origin = "广东省 广州",
            messengerId = shop["wechat"].asText(),
            id = shop["id"].asText(),
            follower = shop["focus_stall"].asText(),
            saleCount = shop["stall_sales"].asText(),
            newProductCount = shop["upshelfItemNum365d"]?.asText(),
            selfPhotoRate = shop["outsideCreditlist"][0]["indicatorsNum"].asText().replace("%",""),
            refundRate = shop["outsideCreditlist"][2]["indicatorsNum"].asText().replace("%",""),
            qualityConfirmRate = shop["outsideCreditlist"][3]["indicatorsNum"].asText().replace("%",""),
            deliveryAccuracyRate = shop["outsideCreditlist"][1]["indicatorsNum"].asText().replace("%",""),
            runningYears = ""
        )
    }

    private fun merchantInfo(infoTitle: String): String {
        val shopDescriptionTitles = multiNodes("body > div.deatil-wrapper > article > aside > div.shop > div.shop-wrapper > dl > dt")

        var titleIndex = -1
        shopDescriptionTitles.forEachIndexed { index, element ->  if(element.text().contains(infoTitle)) titleIndex = index}
        return if (titleIndex < 0) {
            "테스트되지 않은 layout입니다. 개발자에게 문의주세요."
        } else {
            singleNodeValue("body > div.deatil-wrapper > article > aside > div.shop > div.shop-wrapper > dl > dd:nth-child(${titleIndex*2+2})")
        }.trim()
    }

    private fun sizes(): List<Option> {
        return fromScript("var _SIZE = ")
            .map { Option(name = it) }
    }

    private fun fromScript(prefix: String): List<String> {
        return stringFromScript(prefix).split(",")
    }

    private fun stringFromScript(prefix: String): String {
        var text = ""
        document.get().select("script")
            .forEach { script -> script.dataNodes().forEach { node ->
                if(node.wholeData.contains(prefix)) {
                    text =node.wholeData
                }
            }
            }
        return text.split("\n")
            .find { it -> it.contains(prefix) }
            ?.replace(prefix, "")
            ?.replace(";", "")
            ?.trim()
            ?.replace("'", "") ?: ""
    }
}