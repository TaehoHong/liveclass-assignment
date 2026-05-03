package com.futureschole.liveclass.domain.settlement.service

import com.futureschole.liveclass.domain.cancel_record.repository.CancelRecordRepository
import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class SettlementCalculationService(
    private val saleRecordRepository: SaleRecordRepository,
    private val cancelRecordRepository: CancelRecordRepository
) {

    fun calculate(month: YearMonth, creatorId: Long?): List<SettlementMonthlyResponseDto> {
        val creatorIdToSaleRecord = saleRecordRepository.findCreatorIdToSaleRecords(creatorId, month)
        val creatorIdToCancelRecord = cancelRecordRepository.findCreatorIdToCancelRecords(creatorId, month)
        val settlementMonth = month.atDay(1)

        return (creatorIdToSaleRecord.keys + creatorIdToCancelRecord.keys).map { currentCreatorId ->
            val saleRecords = creatorIdToSaleRecord[currentCreatorId].orEmpty()
            val cancelRecords = creatorIdToCancelRecord[currentCreatorId].orEmpty()
            val totalSaleAmount = saleRecords.sumOf { it.amount }
            val totalCancelAmount = cancelRecords.sumOf { it.amount }
            val netSalesAmount = totalSaleAmount - totalCancelAmount
            val commissionAmount = netSalesAmount * COMMISSION_RATE / 100
            val settlementAmount = netSalesAmount - commissionAmount

            SettlementMonthlyResponseDto(
                creatorId = currentCreatorId,
                settlementMonth = settlementMonth,
                totalSaleAmount = totalSaleAmount,
                totalCancelAmount = totalCancelAmount,
                netSalesAmount = netSalesAmount,
                commissionRate = COMMISSION_RATE,
                commissionAmount = commissionAmount,
                settlementAmount = settlementAmount,
                saleCount = saleRecords.size.toLong(),
                cancelCount = cancelRecords.size.toLong(),
                settlementId = null,
                status = null
            )
        }
    }

    companion object {
        const val COMMISSION_RATE: Short = 20
    }
}
