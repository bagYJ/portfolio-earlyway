package com.vicc.scrap.application

import com.vicc.scrap.logger
import io.kotest.core.spec.style.FunSpec
import org.springframework.core.io.DefaultResourceLoader
import java.net.URLConnection
import java.nio.file.Files

//class KtCloudTest: FunSpec({
//    val filesClientExt = FilesClientExt(
//        "cclnccl3@gmail.com",
//        "MTY3MzI0MjM1NjE2NzMyMzgzMDQ0NzM1",
//        "https://ssproxy.ucloudbiz.olleh.com/auth/v1.0",
//        3000
//    )
//    filesClientExt.login()
//    test("업로드") {
//        val file = DefaultResourceLoader().getResource("classpath:images/1662048800726_127781.jpeg").file
//        filesClientExt.storeObject("vvic", file, URLConnection.guessContentTypeFromName(file.getName()))
//        filesClientExt.listObjects("vvic").forEach {
//            logger().info(it.name)
//        }
//    }
//
//})