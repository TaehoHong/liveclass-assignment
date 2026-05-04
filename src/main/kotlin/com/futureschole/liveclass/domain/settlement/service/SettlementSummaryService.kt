package com.futureschole.liveclass.domain.settlement.service

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.settlement.dto.SettlementAmountDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementSummaryItemDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementSummaryResponseDto
import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.time.ZoneId

@Service
class SettlementSummaryService(
    private val settlementRepository: SettlementRepository,
    private val settlementCalculationService: SettlementCalculationService,
) {

    @Transactional(readOnly = true)
    fun summary(startMonth: YearMonth, endMonth: YearMonth): SettlementSummaryResponseDto {
        val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

        validatePeriod(startMonth, endMonth, currentMonth)

        val creatorIdToSettlements = findPastSettlements(startMonth, endMonth, currentMonth)
            .mapValues { it.value.toMutableList() }.toMutableMap()

        if (!startMonth.isAfter(currentMonth) && !endMonth.isBefore(currentMonth)) {
            settlementCalculationService.calculate(currentMonth, creatorId = null)
                .map {
                    SettlementAmountDto(
                        creatorId = it.creatorId,
                        settlementMonth = it.settlementMonth,
                        settlementAmount = it.settlementAmount
                    )
                }
                .forEach { settlement ->
                    creatorIdToSettlements.getOrPut(settlement.creatorId) { mutableListOf() }
                        .add(settlement)
                }
        }

        return creatorIdToSettlements.entries
            .map { (creatorId, rawSettlements) ->
                val settlements = rawSettlements.sortedByDescending { it.settlementMonth }

                SettlementSummaryItemDto(
                    creatorId = creatorId,
                    settlements = settlements,
                    totalSettlementAmount = settlements.sumOf { it.settlementAmount }
                )
            }.let { summaryItem ->
                SettlementSummaryResponseDto(
                    settlements = summaryItem,
                    totalSettlementAmount = summaryItem.sumOf { it.totalSettlementAmount }
                )
            }
    }

    private fun findPastSettlements(
        startMonth: YearMonth,
        endMonth: YearMonth,
        currentMonth: YearMonth
    ): Map<Long, List<SettlementAmountDto>> {
        val pastEndMonth = if (endMonth < currentMonth) endMonth else currentMonth.minusMonths(1)

        if (startMonth.isAfter(pastEndMonth)) {
            return emptyMap()
        }

        return settlementRepository.findCreatorIdToSettlement(startMonth, pastEndMonth)
    }

    private fun validatePeriod(
        startMonth: YearMonth,
        endMonth: YearMonth,
        currentMonth: YearMonth
    ) {
        if (startMonth.isAfter(endMonth)) {
            throw ApiException(ErrorCode.INVALID_DATE_RANGE)
        }

        if (endMonth.isAfter(currentMonth)) {
            throw ApiException(ErrorCode.INVALID_SETTLEMENT_SUMMARY_MONTH_IN_FUTURE)
        }
    }
}
