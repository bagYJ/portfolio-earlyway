package com.vicc.scrap.application

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PapagoTranslateServiceTest : FunSpec({
    val service = PapagoTranslateService()
    test("translate") {
        val translated = service.translate("SIZE INFO 面料：210克-奥代尔纯棉 成份:棉95% 氨纶5% 颜色：黑色 白色 灰色 均码尺寸： 衣长44CM 胸围72-106CM(高弹) 肩宽33CM 袖长16CM （手工测量准许1-3误差） 链接：https://pan.baidu.com/s/1K887jEcStaY9XtoI2b5Jrw 提取码：2yd7 —来自百度网盘超级会员V3的分享 模特展示 MODEL SHOWS")
        translated shouldBe "한국chic 프렌치 키 작은 칼라 캐주얼 더블 트렌치 코트 여"
    }
})
