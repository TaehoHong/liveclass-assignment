package com.futureschole.liveclass.unit.service.settlement

import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import com.futureschole.liveclass.domain.settlement.service.SettlementCalculationService
import com.futureschole.liveclass.domain.settlement.service.SettlementService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class SettlementServiceTest: BehaviorSpec({
    val settlementRepository = mockk<SettlementRepository>()
    val settlementCalculationService = mockk<SettlementCalculationService>()
    val settlementService = SettlementService(settlementRepository, settlementCalculationService)

    afterEach { clearAllMocks() }

    Given("현재월 정산 조회 요청이 있을 때") {
        val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

        listOf(null, currentMonth).forEach { inputMonth ->
            When("month가 ${inputMonth ?: "null"}이면") {
                Then("repository 결과 앞에 현재월 예상 정산을 붙여 반환한다") {
                    val storedSettlement = settlement(id = 1L, settlementMonth = currentMonth.atDay(1))
                    val storedResponse = SettlementMonthlyResponseDto(storedSettlement)
                    val predictedResponse = predictedSettlement(currentMonth)

                    every { settlementRepository.findAll(1L, inputMonth) } returns listOf(storedSettlement)
                    every { settlementCalculationService.calculate(currentMonth, 1L) } returns listOf(predictedResponse)

                    val result = settlementService.findAll(
                        month = inputMonth,
                        creatorId = 1L
                    )

                    result shouldBe listOf(predictedResponse, storedResponse)

                    verify(exactly = 1) { settlementCalculationService.calculate(currentMonth, 1L) }
                }
            }
        }
    }

    Given("과거월 정산 조회 요청이 있을 때") {
        When("정산 목록을 조회하면") {
            Then("예상 정산 계산을 호출하지 않고 repository 결과만 반환한다") {
                val pastMonth = YearMonth.of(2025, 3)
                val storedSettlement = settlement(id = 1L, settlementMonth = pastMonth.atDay(1))

                every { settlementRepository.findAll(1L, pastMonth) } returns listOf(storedSettlement)

                val result = settlementService.findAll(
                    month = pastMonth,
                    creatorId = 1L
                )

                result shouldBe listOf(SettlementMonthlyResponseDto(storedSettlement))
                verify(exactly = 0) { settlementCalculationService.calculate(any(), any()) }
            }
        }
    }
})

private fun settlement(
    id: Long,
    settlementMonth: LocalDate,
    creatorId: Long = 1L
): Settlement {
    return Settlement(
        id = id,
        creatorId = creatorId,
        status = SettlementStatus.PENDING,
        totalSaleAmount = 100000L,
        totalCancelAmount = 20000L,
        netSalesAmount = 80000L,
        settlementAmount = 64000L,
        saleCount = 2L,
        cancelCount = 1L,
        commissionRate = 20.toShort(),
        commissionAmount = 16000L,
        carryoverDeductionAmount = 0L,
        settlementMonth = settlementMonth,
        settledAt = LocalDateTime.of(2025, 4, 1, 10, 0),
        confirmedAt = null,
        paidAt = null,
        createdAt = LocalDateTime.of(2025, 4, 1, 10, 0)
    )
}

private fun predictedSettlement(month: YearMonth): SettlementMonthlyResponseDto {
    return SettlementMonthlyResponseDto(
        creatorId = 1L,
        settlementMonth = month.atDay(1),
        settlementId = null,
        status = null,
        totalSaleAmount = 50000L,
        totalCancelAmount = 0L,
        netSalesAmount = 50000L,
        commissionRate = 20.toShort(),
        commissionAmount = 10000L,
        settlementAmount = 40000L,
        saleCount = 1L,
        cancelCount = 0L
    )
}
