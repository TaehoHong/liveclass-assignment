package com.futureschole.liveclass.domain.settlement.service

import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

}
