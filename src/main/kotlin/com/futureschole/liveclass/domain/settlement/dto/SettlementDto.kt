package com.futureschole.liveclass.domain.settlement.dto

import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import com.querydsl.core.annotations.QueryProjection
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

data class SettlementSummaryResponseDto(
    val settlements: List<SettlementSummaryItemDto>,
    val totalSettlementAmount: Long
)

data class SettlementSummaryItemDto(
    val creatorId: Long,
    val settlements: List<SettlementAmountDto>,
    val totalSettlementAmount: Long
)

data class SettlementAmountDto @QueryProjection constructor(
    val creatorId: Long,
    val settlementMonth: LocalDate,
    val settlementAmount: Long
)
