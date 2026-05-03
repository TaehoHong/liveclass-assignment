package com.futureschole.liveclass.domain.settlement.dto

import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import java.time.LocalDate

data class SettlementMonthlyResponseDto(
    val creatorId: Long,
    val settlementMonth: LocalDate,
    val settlementId: Long?,
    val status: SettlementStatus?,
    val totalSaleAmount: Long,
    val totalCancelAmount: Long,
    val netSalesAmount: Long,
    val commissionRate: Short,
    val commissionAmount: Long,
    val settlementAmount: Long,
    val saleCount: Long,
    val cancelCount: Long
) {
    constructor(settlement: Settlement) : this(
        creatorId = settlement.creatorId,
        settlementMonth = settlement.settlementMonth,
        settlementId = settlement.id,
        status = settlement.status,
        totalSaleAmount = settlement.totalSaleAmount,
        totalCancelAmount = settlement.totalCancelAmount,
        netSalesAmount = settlement.netSalesAmount,
        commissionRate = settlement.commissionRate,
        commissionAmount = settlement.commissionAmount,
        settlementAmount = settlement.settlementAmount,
        saleCount = settlement.saleCount,
        cancelCount = settlement.cancelCount
    )
}
