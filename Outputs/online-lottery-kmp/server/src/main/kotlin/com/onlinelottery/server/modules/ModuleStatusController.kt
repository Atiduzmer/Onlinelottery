package com.onlinelottery.server.modules

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class ModuleStatus(
    val code: String,
    val name: String,
    val phase: String,
    val capabilities: List<String>,
)

@RestController
@RequestMapping("/api/v1/public/modules")
class ModuleStatusController {
    @GetMapping
    fun listModules(): List<ModuleStatus> = listOf(
        ModuleStatus("identity", "账户与实名", "SCHEMA_READY", listOf("用户", "实名", "设备", "银行卡", "风控画像")),
        ModuleStatus("catalog", "彩种与赛事", "API_READY", listOf("彩种目录", "玩法", "期次", "赛事", "盘口赔率")),
        ModuleStatus("betting", "投注与出票", "SCHEMA_READY", listOf("幂等下单", "投注明细", "出票", "状态轨迹")),
        ModuleStatus("wallet", "钱包与支付", "SCHEMA_READY", listOf("分户账", "双录分录", "充值", "提现", "回调去重")),
        ModuleStatus("settlement", "结算与派奖", "SCHEMA_READY", listOf("结算批次", "派奖", "退款")),
        ModuleStatus("operations", "风控与运营", "SCHEMA_READY", listOf("规则", "事件", "人工审核", "通知", "审计")),
    )
}
