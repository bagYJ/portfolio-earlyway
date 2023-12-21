package com.vicc.scrap.infra.api

import com.vicc.scrap.application.*
import com.vicc.scrap.domain.DuplicateException
import com.vicc.scrap.infra.repository.GodoProductRepositoryImpl
import com.vicc.scrap.logger
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFDrawing
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import java.net.URL
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.Path


@Controller
class ScrapController(
    private val scrapService: ScrapService,
    private val productTranslateService: ProductTranslateService,
    private val productService: ProductService,
    private val godoMallService: GodoMallService,
    private val godoProductRepositoryImpl: GodoProductRepositoryImpl
) {
    @PostMapping("/products")
    fun registerProducts(@ModelAttribute("excelDownload") stringParam: StringParam, model: Model): String {
        val ids = stringParam.str.split(",").map { it.trim() }
        val failedIds: MutableList<String> = mutableListOf()
        val duplicateIds: MutableList<String> = mutableListOf()
        ids.forEach {id ->
            try {
                godoProductRepositoryImpl.getGoodsNo(id)
                scrapService.productById(id).apply { productService.save(this) }

                productService.findById(id, Locale.KOREA)?.let {
                    productService.upload(it)
                    productService.saveExternalService(it)
                }
            } catch (e: DuplicateException) {
                duplicateIds.add(id)
            } catch (e: Exception) {
                logger().info(e.message)
                failedIds.add(id)
            }
        }

        model.addAttribute("failedIds", failedIds.joinToString(", "))
        model.addAttribute("successIds", ((ids - failedIds.toSet()) - duplicateIds.toSet()).joinToString(", "))
        model.addAttribute("duplicateIds", duplicateIds.joinToString(", "))

        return "products"
//        println(failedIds.joinToString(","))
    }

    @GetMapping("/upsert-product")
    fun upsertProduct(mode: Model): String {
        mode.addAttribute(StringParam(str = ""))
        return "upsert-product"
    }

    @GetMapping("/download")
    fun downloadPage(mode: Model): String {
        mode.addAttribute(StringParam(str = ""))
        return "download"
    }

    @GetMapping("/test")
    fun test(mode: Model): String {
        godoMallService.resize(Path("/Users/owin/Owin/devel/scrap/register_magnify_097.jpg"), 500, true)
        return "test"
    }

    @PostMapping("/downloadExcel")
    fun downloadExcel(@ModelAttribute("excelDownload") stringParam: StringParam, response: HttpServletResponse) {
        val imageSize: Short = 2600
        logger().info("urls = $stringParam")
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("products")
        sheet.setColumnWidth(1, 6000)
        sheet.setColumnWidth(2, 2000)
        var rowNum = 0
        var row = sheet.createRow(rowNum++)
        var cell: XSSFCell? = null
        var cellNum = 0
        // Header
        cell = row.createCell(cellNum)
        cell.setCellValue("NO")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("사진")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("바이어 바코드")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("바이어 스타일넘버")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("바이어 상품명")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("매장 색상")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("매장 스타일 번호")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("매장 사이즈")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("바이어 구매 색상")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("바이어 구매 사이즈")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("주문 수량")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("링크")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("주소")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("전화번호")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("사입가")
        cellNum = ++cellNum
        cell = row.createCell(cellNum)
        cell.setCellValue("금액")

        val products = stringParam.str.split(",")
            .filter { it.isNotEmpty() && it.isNotBlank() }
            .map { it.trim() }
            .map { url -> scrapService.product(url) }
            .map { product -> productTranslateService.translate(product) }

        products.forEachIndexed { index, product ->
            logger().info("product -> $product")

            row = sheet.createRow(rowNum++)
            row.height = imageSize

            cell = row.createCell(0)
            cell!!.setCellValue((index + 1).toString())

            val ins = URL(product.images.first().split("?").first()).openStream()
            val imageId = wb.addPicture(IOUtils.toByteArray(ins), Workbook.PICTURE_TYPE_PNG)
            val drawing = sheet.createDrawingPatriarch() as XSSFDrawing
            val anchor = XSSFClientAnchor()
            anchor.setCol1(1)
            anchor.setCol2(2)
            anchor.row1 = rowNum-1
            anchor.row2 = rowNum
            drawing.createPicture(anchor, imageId)

            cell = row.createCell(4)
            cell!!.setCellValue(product.name)

            cell = row.createCell(5)
            cell!!.setCellValue(product.colors.map { it.name }.joinToString(separator = "/"))

            cell = row.createCell(6)
            cell!!.setCellValue(product.shopStyleNumber)

            cell = row.createCell(7)
            cell!!.setCellValue(product.sizes.map { it.name }.joinToString(separator = "/"))

            cell = row.createCell(11)
            cell!!.setCellValue(product.linkUrl)

            cell = row.createCell(12)
            cell!!.setCellValue(product.shop.address)

            cell = row.createCell(13)
            cell!!.setCellValue(product.shop.phoneNumber)

            cell = row.createCell(14)
            val priceText = if (product.price.to == null) {
                product.price.from.toString()
            } else {
                product.price.from.toString() + " - " + product.price.to.toString()
            }
            cell!!.setCellValue(priceText)
        }

        // 컨텐츠 타입과 파일명 지정
        response.contentType = "ms-vnd/excel"
        response.setHeader("Content-Disposition", "attachment;filename=vvic_${LocalDateTime.now()}.xlsx")

        // Excel File Output
        wb.write(response.outputStream)
        wb.close()
    }
}

data class StringParam(
    val str: String
)