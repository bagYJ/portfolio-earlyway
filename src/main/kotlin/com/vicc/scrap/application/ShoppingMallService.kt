package com.vicc.scrap.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.vicc.scrap.domain.Description
import com.vicc.scrap.domain.DescriptionRow
import com.vicc.scrap.domain.ImageTranslate
import com.vicc.scrap.domain.Product
import com.vicc.scrap.logger
import org.apache.commons.net.ftp.FTPClient
import org.imgscalr.Scalr
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists

interface ShoppingMallService {
    fun save(product: Product)
}

@Service
class GodoMallService: ShoppingMallService {

//    private val filesClientExt = FilesClientExt(
//        "cclnccl3@gmail.com",
//        "MTY3MzI0MjM1NjE2NzMyMzgzMDQ0NzM1",
//        "https://ssproxy.ucloudbiz.olleh.com/auth/v1.0",
//        3000
//    )

    private val restTemplate = RestTemplateBuilder()
        .rootUri("https://naveropenapi.apigw.ntruss.com")
        .defaultHeader("Content-Type", "multipart/form-data; charset=UTF-8")
        .defaultHeader("X-NCP-APIGW-API-KEY-ID", "X-NCP-APIGW-API-KEY-ID")
        .defaultHeader("X-NCP-APIGW-API-KEY", "X-NCP-APIGW-API-KEY")
        .build()
    private val restTranslateTemplate = RestTemplateBuilder()
        .rootUri("https://api.ohoolabs.com")
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("X-OHOO-API-KEY", "c0vxVgpd/VDBuqRc/DXxUllKx0RxELf1dDAc7qvjSMM=")
        .build()


    fun getFtpConnection(): FTPClient {

        val client = FTPClient()
        client.connect("121.143.79.109", 21)
        val isLogin = client.login("username", "password")
        client.enterLocalPassiveMode()
        // 이 옵션 없으면 이미지 깨져서 올라감
        client.setFileType(FTPClient.BINARY_FILE_TYPE)
        return client
    }


    private fun rootPath(id: String): String {
        return "/public_html/goods/images/${id}"
    }

    private fun tempPath(): String = System.getProperty("java.io.tmpdir")

    private fun downloadFile(url: String, resize: Boolean, widthSize: Int = 600, cropFl: Boolean? = true, productFl: Boolean? = false): String {
        val localPath = Paths.get("${tempPath()}/${fileNameFromUrl(url)}")
        logger().info("local path = $localPath")
        localPath.deleteIfExists()
        try {
            URL(url).openStream().use { Files.copy(it, localPath) }
            if (resize) {
                resize(localPath, widthSize, cropFl, productFl)
            }
        }catch (_: java.nio.file.FileAlreadyExistsException) {

        }

        return localPath.toString()
    }

    fun resize(path: Path, widthSize: Int = 600, cropFl: Boolean? = true, productFl: Boolean? = false) {
        var image = ImageIO.read(path.toFile())
        if(cropFl === true && image.width < image.height ) {
            println(image)
            image = image.getSubimage(0, (image.height-image.width)/2, image.width, image.width)
            path.toFile().delete()
            ImageIO.write(image, path.toFile().extension, path.toFile())
        }
        if (productFl === true && image.width < 500) {
            var modWidth = 500
            var modHeight = (500 * image.height) / image.width
            var modImage = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, modWidth, modHeight, Scalr.OP_ANTIALIAS)
            ImageIO.write(modImage, path.toFile().extension, path.toFile())
        }
        val needResize = image.width > widthSize
        if(needResize) {
            var width = if(image.width > widthSize) widthSize else image.width
            val height = if(width != image.height) width else image.height
            println("${width}, ${height}")
            val resizedImage = if (cropFl === true) {
                Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, height, Scalr.OP_ANTIALIAS)
            } else {
                width = if (height > 1900) {
                    (height * width) / 1900
                } else {
                    width
                }
                Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width)
            }
            path.toFile().delete()
            ImageIO.write(resizedImage, path.toFile().extension, path.toFile())
        }
    }

    private fun deleteFile(path: String) {
        Paths.get(path).deleteIfExists()
    }

    private fun fileNameFromUrl(url: String): String {
        return try {
            url.split(".+\\/".toRegex())[1]
        }catch (e: Exception) {
            logger().error("invalid url: $url")
            if (!url.isNullOrBlank()) {
                url
            } else {
                throw e
            }
        }
    }


    override fun save(product: Product) {
        uploadProductImage(product)
        uploadDetailImage(product)
    }

    fun uploadDetailImage(product: Product) {
        product.description?.rows?.let { rows ->
            product.description = Description(rows =  rows.map {
                if(it.rowType == DescriptionRow.RowTypes.String) {
                    it
                }else {
                    DescriptionRow(rowType = DescriptionRow.RowTypes.Image, row = uploadImages(listOf(it.row), product.id, true  , 960, false, false).first())
                }
            }.toMutableList())
        }
        println(product.description)
        val imageList: MutableList<String> = mutableListOf()
//        val imageList: MutableList<BufferedImage> = mutableListOf()
//        val imageWidth: MutableList<Int> = mutableListOf()
        product.description?.rows?.map {
            if(it.rowType == DescriptionRow.RowTypes.Image) {
                imageList.add("/tmp/${fileNameFromUrl(it.row)}")
//                imageList.add(ImageIO.read(File("/tmp/${fileNameFromUrl(it.row)}")))
//                imageWidth.add(ImageIO.read(File("/tmp/${fileNameFromUrl(it.row)}")).width)
            }
        }

        val translateImage: MutableList<String> = mutableListOf()
        imageList.mapIndexed { index, it ->
            val filePath = "/tmp/${product.id}_${index}.png"
//            val mergeImage = BufferedImage(it.width, it.height, BufferedImage.TYPE_INT_BGR)
//            val graphics = mergeImage.graphics
//            graphics?.drawImage(it, 0, 0, Color.WHITE, null)
//            ImageIO.write(mergeImage, "png", File(filePath))
//
//            val multipartBodyBuilder: MultipartBodyBuilder = MultipartBodyBuilder()
//            multipartBodyBuilder.part("source", "zh-CN")
//            multipartBodyBuilder.part("target", "ko")
//            multipartBodyBuilder.part("image", FileSystemResource(filePath))
//            val multipartBody = multipartBodyBuilder.build()
            try {
//                val response = restTemplate.postForEntity("/image-to-image/v1/translate", multipartBody, Map::class.java)
//                val renderedImage = (response.body!!["data"] as Map<String, String>)["renderedImage"] as String
//                val image = BufferedImage(it.width, it.height, BufferedImage.TYPE_INT_BGR)
//                val graphic = image.graphics
//                val imageByte = DatatypeConverter.parseBase64Binary(renderedImage)
//                graphic.drawImage(ImageIO.read(ByteArrayInputStream(imageByte)), 0, 0, null)
                val response = restTranslateTemplate.postForEntity("/studio/v1/image/translate", ObjectMapper().writeValueAsString(ImageTranslate.copy(
                    path = it,
                    source = listOf("zh"),
                    target = "ko"
                )), String::class.java)
                val responses = ObjectMapper().readTree(response.body)
                println("image translate: $responses")
                val url = URL(responses["responses"][0]["uri"].textValue())
                ImageIO.write(ImageIO.read(url), "png", File(filePath))
                translateImage.add(filePath)
            } catch (e: Exception) {
                logger().info(e.message)
                ImageIO.write(ImageIO.read(File(it)), "png", File(filePath))
                translateImage.add(filePath)
            }
        }

        product.description?.rows?.let { rows ->
            val descript = rows.filter {
                it.rowType == DescriptionRow.RowTypes.String
            }.map {
                it
            }.toMutableList()
            translateImage.map {
                descript.add(
                    DescriptionRow(rowType = DescriptionRow.RowTypes.Image, row = uploadTransImages(listOf(it), product.id).first())
                )
                deleteFile(it)
            }
            product.description = Description(rows = descript)
        }
        println(product.description)
    }

    fun uploadProductImage(product: Product) {
        product.images = uploadImages(productId = product.id, imageUrls = product.images, resize = true, productFl = true)
    }

    private fun uploadTransImages(imageUrls: List<String>, productId: String): List<String> {
        if(imageUrls.isEmpty()) return emptyList()

        val connection = getFtpConnection()
        val root = rootPath(productId)

        try {
            connection.mkd(root)
            imageUrls.forEach { localPath ->
                logger().info("uploadFile -> $localPath")
                connection.storeFile("$root/" + fileNameFromUrl(localPath), File(localPath).inputStream())
                logger().info("upload complete -> $localPath" )
                deleteFile(localPath)
            }

        }catch (_: MalformedURLException){

        } catch (e: Exception) {
            e.printStackTrace()

            throw e
        }finally {
            connection.disconnect()
        }
        return imageUrls.map { "$root/".replace("/public_html", "") + fileNameFromUrl(it) }
    }

    private fun uploadImages(imageUrls: List<String>, productId: String, resize: Boolean? = false, widthSize: Int? = 600, delete: Boolean? = true, cropFl: Boolean? = true, productFl: Boolean? = false): List<String> {
        if(imageUrls.isEmpty()) return emptyList()

        val connection = getFtpConnection()
        val root = rootPath(productId)

        try {
            connection.mkd(root)
//            val storedFileNames = connection.listFiles(root).filter { it.isFile }.map { it.name }
            val storedFileNames = emptyList<String>()
            imageUrls
                .filter { !storedFileNames.contains(fileNameFromUrl(it)) }
                .map {
                    downloadFile(if (it.startsWith("https")) {
                        it
                    } else {
                        "https:" + it
                    }, resize = resize ?: false, widthSize!!, cropFl, productFl)
                }.forEach { localPath ->
                    logger().info("uploadFile -> $localPath")
                    connection.storeFile("$root/" + fileNameFromUrl(localPath), File(localPath).inputStream())
                    logger().info("upload complete -> $localPath" )
                    if (delete === true) {
                        deleteFile(localPath)
                    }
                }

        }catch (_: MalformedURLException){

        } catch (e: Exception) {
            e.printStackTrace()

            throw e
        }finally {
            connection.disconnect()
        }
        return imageUrls.map { "$root/".replace("/public_html", "") + fileNameFromUrl(it) }
    }
//    private fun initConnection() {
//        connection = getFtpConnection()
//    }
}
