package com.futureschole.liveclass.unit.service.settlement

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.settlement.dto.SettlementAmountDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import com.futureschole.liveclass.domain.settlement.service.SettlementCalculationService
import com.futureschole.liveclass.domain.settlement.service.SettlementSummaryService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.YearMonth
import java.time.ZoneId

class SettlementSummaryServiceTest: BehaviorSpec({
    val settlementRepository = mockk<SettlementRepository>()
    val settlementCalculationService = mockk<SettlementCalculationService>()
    val settlementSummaryService = SettlementSummaryService(settlementRepository, settlementCalculationService)

    afterEach { clearAllMocks() }

    Given("과거월 정산 예정 금액이 있을 때") {
        When("과거월 범위로 summary를 조회하면") {
            Then("저장 정산만 creator별 목록과 합계로 반환한다") {
                val startMonth = YearMonth.of(2025, 3)
                val endMonth = YearMonth.of(2025, 4)
                val marchSettlement = settlementAmount(creatorId = 1L, month = startMonth, amount = 64000L)
                val aprilSettlement = settlementAmount(creatorId = 1L, month = endMonth, amount = 56000L)

                every {
                    settlementRepository.findCreatorIdToSettlement(startMonth, endMonth)
                } returns mapOf(1L to listOf(marchSettlement, aprilSettlement))

                val result = settlementSummaryService.summary(startMonth, endMonth)

                result.settlements.size shouldBe 1
                result.settlements[0].creatorId shouldBe 1L
                result.settlements[0].settlements shouldBe listOf(aprilSettlement, marchSettlement)
                result.settlements[0].totalSettlementAmount shouldBe 120000L
                result.totalSettlementAmount shouldBe 120000L

                verify(exactly = 1) { settlementRepository.findCreatorIdToSettlement(startMonth, endMonth) }
                verify(exactly = 0) { settlementCalculationService.calculate(any(), any()) }
            }
        }
    }

    Given("현재월 예상 정산이 있을 때") {
        When("현재월만 summary를 조회하면") {
            Then("계산 결과만 creator별 목록과 합계로 반환한다") {
                val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))
                val calculatedSettlement = predictedSettlement(
                    creatorId = 1L,
                    month = currentMonth,
                    settlementAmount = 104000L
                )

                every {
                    settlementCalculationService.calculate(currentMonth, creatorId = null)
                } returns listOf(calculatedSettlement)

                val result = settlementSummaryService.summary(currentMonth, currentMonth)

                result.settlements.size shouldBe 1
                result.settlements[0].creatorId shouldBe 1L
                result.settlements[0].settlements shouldBe listOf(
                    settlementAmount(creatorId = 1L, month = currentMonth, amount = 104000L)
                )
                result.settlements[0].totalSettlementAmount shouldBe 104000L
                result.totalSettlementAmount shouldBe 104000L

                verify(exactly = 0) { settlementRepository.findCreatorIdToSettlement(any(), any()) }
                verify(exactly = 1) { settlementCalculationService.calculate(currentMonth, creatorId = null) }
            }
        }
    }

    Given("과거월 저장 정산과 현재월 예상 정산이 함께 있을 때") {
        When("과거월부터 현재월까지 summary를 조회하면") {
            Then("같은 creator의 월별 정산을 합쳐 반환한다") {
                val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))
                val pastMonth = currentMonth.minusMonths(1)
                val pastSettlement = settlementAmount(creatorId = 1L, month = pastMonth, amount = 64000L)
                val currentSettlement = predictedSettlement(
                    creatorId = 1L,
                    month = currentMonth,
                    settlementAmount = 104000L
                )

                every {
                    settlementRepository.findCreatorIdToSettlement(pastMonth, pastMonth)
                } returns mapOf(1L to listOf(pastSettlement))
                every {
                    settlementCalculationService.calculate(currentMonth, creatorId = null)
                } returns listOf(currentSettlement)

                val result = settlementSummaryService.summary(pastMonth, currentMonth)

                result.settlements.size shouldBe 1
                result.settlements[0].creatorId shouldBe 1L
                result.settlements[0].settlements shouldBe listOf(
                    settlementAmount(creatorId = 1L, month = currentMonth, amount = 104000L),
                    pastSettlement
                )
                result.settlements[0].totalSettlementAmount shouldBe 168000L
                result.totalSettlementAmount shouldBe 168000L
            }
        }
    }

    Given("여러 creator의 정산 예정 금액이 섞여 있을 때") {
        When("summary를 조회하면") {
            Then("creator별로 분리하고 각 합계를 계산한다") {
                val startMonth = YearMonth.of(2025, 3)
                val endMonth = YearMonth.of(2025, 4)
                val creator1Settlement = settlementAmount(creatorId = 1L, month = startMonth, amount = 64000L)
                val creator2MarchSettlement = settlementAmount(creatorId = 2L, month = startMonth, amount = 32000L)
                val creator2AprilSettlement = settlementAmount(creatorId = 2L, month = endMonth, amount = 48000L)

                every {
                    settlementRepository.findCreatorIdToSettlement(startMonth, endMonth)
                } returns mapOf(
                    1L to listOf(creator1Settlement),
                    2L to listOf(creator2MarchSettlement, creator2AprilSettlement)
                )

                val result = settlementSummaryService.summary(startMonth, endMonth)
                val creatorIdToItem = result.settlements.associateBy { it.creatorId }

                creatorIdToItem.keys shouldBe setOf(1L, 2L)
                creatorIdToItem.getValue(1L).settlements shouldBe listOf(creator1Settlement)
                creatorIdToItem.getValue(1L).totalSettlementAmount shouldBe 64000L
                creatorIdToItem.getValue(2L).settlements shouldBe listOf(creator2AprilSettlement, creator2MarchSettlement)
                creatorIdToItem.getValue(2L).totalSettlementAmount shouldBe 80000L
                result.totalSettlementAmount shouldBe 144000L
            }
        }
    }

    Given("같은 creator의 월별 정산 순서가 섞여 있을 때") {
        When("summary를 조회하면") {
            Then("월 내림차순으로 정렬해 반환한다") {
                val startMonth = YearMonth.of(2025, 3)
                val endMonth = YearMonth.of(2025, 4)
                val marchSettlement = settlementAmount(creatorId = 1L, month = startMonth, amount = 64000L)
                val aprilSettlement = settlementAmount(creatorId = 1L, month = endMonth, amount = 56000L)

                every {
                    settlementRepository.findCreatorIdToSettlement(startMonth, endMonth)
                } returns mapOf(1L to listOf(aprilSettlement, marchSettlement))

                val result = settlementSummaryService.summary(startMonth, endMonth)

                result.settlements[0].settlements shouldBe listOf(aprilSettlement, marchSettlement)
            }
        }
    }

    Given("기간 내 정산 예정 금액이 없을 때") {
        When("summary를 조회하면") {
            Then("빈 목록과 0원 합계를 반환한다") {
                val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

                every { settlementCalculationService.calculate(currentMonth, creatorId = null) } returns emptyList()

                val result = settlementSummaryService.summary(currentMonth, currentMonth)

                result.settlements shouldBe emptyList()
                result.totalSettlementAmount shouldBe 0L

                verify(exactly = 0) { settlementRepository.findCreatorIdToSettlement(any(), any()) }
                verify(exactly = 1) { settlementCalculationService.calculate(currentMonth, creatorId = null) }
            }
        }
    }

    Given("시작월이 종료월보다 뒤인 summary 요청이 있을 때") {
        When("summary를 조회하면") {
            Then("날짜 범위 오류를 반환하고 조회와 계산을 하지 않는다") {
                val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

                val exception = shouldThrow<ApiException> {
                    settlementSummaryService.summary(currentMonth, currentMonth.minusMonths(1))
                }

                exception.errorCode shouldBe ErrorCode.INVALID_DATE_RANGE

                verify(exactly = 0) {
                    settlementRepository.findCreatorIdToSettlement(any(), any())
                    settlementCalculationService.calculate(any(), any())
                }
            }
        }
    }

    Given("미래월이 포함된 summary 요청이 있을 때") {
        When("summary를 조회하면") {
            Then("미래월 오류를 반환하고 조회와 계산을 하지 않는다") {
                val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

                val exception = shouldThrow<ApiException> {
                    settlementSummaryService.summary(currentMonth, currentMonth.plusMonths(1))
                }

                exception.errorCode shouldBe ErrorCode.INVALID_SETTLEMENT_SUMMARY_MONTH_IN_FUTURE

                verify(exactly = 0) {
                    settlementRepository.findCreatorIdToSettlement(any(), any())
                    settlementCalculationService.calculate(any(), any())
                }
            }
        }
    }
})

private fun settlementAmount(
    creatorId: Long,
    month: YearMonth,
    amount: Long
): SettlementAmountDto {
    return SettlementAmountDto(
        creatorId = creatorId,
        settlementMonth = month.atDay(1),
        settlementAmount = amount
    )
}

private fun predictedSettlement(
    creatorId: Long,
    month: YearMonth,
    settlementAmount: Long
): SettlementMonthlyResponseDto {
    return SettlementMonthlyResponseDto(
        creatorId = creatorId,
        settlementMonth = month.atDay(1),
        settlementId = null,
        status = null,
        totalSaleAmount = settlementAmount,
        totalCancelAmount = 0L,
        netSalesAmount = settlementAmount,
        commissionRate = 20.toShort(),
        commissionAmount = 0L,
        settlementAmount = settlementAmount,
        saleCount = 1L,
        cancelCount = 0L
    )
}
