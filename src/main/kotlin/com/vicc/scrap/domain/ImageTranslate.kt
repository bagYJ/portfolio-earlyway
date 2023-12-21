package com.vicc.scrap.domain

import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*

data class ImageTranslate(
    val requests: List<Requests>
) {
    data class Requests(
        val requestId: String,
        val image: Image,
        val features: List<Features>
    ) {
        data class Image(
            val name: String,
            val data: String
        )
        data class Features(
            val type: String,
            val source: Source,
            val target: Target
        ) {
            data class Source(
                val languages: List<String>
            )
            data class Target(
                val language: String
            )
        }
    }

    companion object {
        fun copy(path: String, source: List<String>, target: String): ImageTranslate {
            val file = File(path)
            return ImageTranslate(
                requests = listOf(Requests(
                    requestId = file.name,
                    image = Requests.Image(
                        name = file.name,
                        data = "data:image/${file.name.substring(file.name.lastIndexOf(".") + 1)};base64,${Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file))}"
                    ),
                    features = listOf(Requests.Features(
                        type = "DOCUMENT_TEXT_DETECTION",
                        source = Requests.Features.Source(
                            languages = source
                        ),
                        target = Requests.Features.Target(
                            language = target
                        )
                    ))
                ))
            )
        }
    }
}
