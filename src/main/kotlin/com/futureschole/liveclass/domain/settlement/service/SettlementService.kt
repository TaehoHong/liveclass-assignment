package com.futureschole.liveclass.domain.settlement.service

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.settlement.dto.CreationSettlementDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

@Service
class SettlementService(
    private val settlementRepository: SettlementRepository,
    private val settlementCalculationService: SettlementCalculationService
) {

    @Transactional(readOnly = true)
    fun findAll(month: YearMonth?, creatorId: Long?): List<SettlementMonthlyResponseDto> {
        val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

        val response = settlementRepository.findAll(creatorId, month)
            .map { SettlementMonthlyResponseDto(it) }

        if (month == null || month == currentMonth) {
            val predictedSettlement = settlementCalculationService.calculate(currentMonth, creatorId)

            return (predictedSettlement + response)
        }
        return response
    }

    @Transactional
    fun create(input: CreationSettlementDto): SettlementMonthlyResponseDto {
        val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

        if (!input.month.isBefore(currentMonth)) {
            throw ApiException(ErrorCode.INVALID_SETTLEMENT_MONTH_NOT_CLOSED)
        }

        if (settlementRepository.existsByCreatorIdAndSettlementMonth(input.creatorId, input.month.atDay(1))) {
            throw ApiException(ErrorCode.DUPLICATE_SETTLEMENT)
        }

        val settlement = settlementCalculationService.calculate(input.month, input.creatorId)
            .firstOrNull()
            ?.let { calculatedSettlement ->
                val prevSettlementAmount =
                    settlementRepository.findAmountByCreatorIdAndSettlementMonth(input.creatorId, input.month)
                val carryoverDeductionAmount = minOf(prevSettlementAmount ?: 0L, 0L)
                createSettlement(calculatedSettlement, carryoverDeductionAmount)
            } ?: let { Settlement.empty(creatorId = input.creatorId, month = input.month) }

        return settlement.runCatching { settlementRepository.saveAndFlush(this) }
            .getOrElse { throw ApiException(ErrorCode.DUPLICATE_SETTLEMENT) }
            .let { SettlementMonthlyResponseDto(it) }
    }

    private fun createSettlement(
        calculatedSettlement: SettlementMonthlyResponseDto,
        carryoverDeductionAmount: Long,
    ): Settlement = Settlement(
        id = 0L,
        creatorId = calculatedSettlement.creatorId,
        status = SettlementStatus.PENDING,
        totalSaleAmount = calculatedSettlement.totalSaleAmount,
        totalCancelAmount = calculatedSettlement.totalCancelAmount,
        netSalesAmount = calculatedSettlement.netSalesAmount,
        settlementAmount = calculatedSettlement.settlementAmount - carryoverDeductionAmount,
        saleCount = calculatedSettlement.saleCount,
        cancelCount = calculatedSettlement.cancelCount,
        commissionRate = calculatedSettlement.commissionRate,
        commissionAmount = calculatedSettlement.commissionAmount,
        carryoverDeductionAmount = carryoverDeductionAmount,
        settlementMonth = calculatedSettlement.settlementMonth,
        settledAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
    )
}
