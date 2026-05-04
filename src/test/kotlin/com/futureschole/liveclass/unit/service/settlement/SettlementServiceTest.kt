package com.futureschole.liveclass.unit.service.settlement

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.settlement.dto.CreationSettlementDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import com.futureschole.liveclass.domain.settlement.service.SettlementCalculationService
import com.futureschole.liveclass.domain.settlement.service.SettlementService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.dao.DataIntegrityViolationException
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

    Given("계산 결과가 있는 닫힌 월 정산 생성 요청이 있을 때") {
        When("정산을 생성하면") {
            Then("계산 결과를 PENDING 정산으로 저장한다") {
                val month = YearMonth.of(2025, 3)
                val input = CreationSettlementDto(creatorId = 1L, month = month)
                val calculatedSettlement = predictedSettlement(month)
                val savedSettlement = slot<Settlement>()

                every {
                    settlementRepository.existsByCreatorIdAndSettlementMonth(1L, month.atDay(1))
                } returns false
                every { settlementCalculationService.calculate(month, 1L) } returns listOf(calculatedSettlement)
                every { settlementRepository.findAmountByCreatorIdAndSettlementMonth(1L, month) } returns null
                every { settlementRepository.saveAndFlush(capture(savedSettlement)) } answers { savedSettlement.captured }

                val result = settlementService.create(input)

                savedSettlement.captured.creatorId shouldBe 1L
                savedSettlement.captured.status shouldBe SettlementStatus.PENDING
                savedSettlement.captured.settlementMonth shouldBe month.atDay(1)
                savedSettlement.captured.totalSaleAmount shouldBe calculatedSettlement.totalSaleAmount
                savedSettlement.captured.totalCancelAmount shouldBe calculatedSettlement.totalCancelAmount
                savedSettlement.captured.netSalesAmount shouldBe calculatedSettlement.netSalesAmount
                savedSettlement.captured.commissionAmount shouldBe calculatedSettlement.commissionAmount
                savedSettlement.captured.settlementAmount shouldBe calculatedSettlement.settlementAmount
                savedSettlement.captured.saleCount shouldBe calculatedSettlement.saleCount
                savedSettlement.captured.cancelCount shouldBe calculatedSettlement.cancelCount
                savedSettlement.captured.carryoverDeductionAmount shouldBe 0L

                result.status shouldBe SettlementStatus.PENDING
                result.settlementAmount shouldBe calculatedSettlement.settlementAmount
            }
        }
    }

    Given("계산 결과가 없는 닫힌 월 정산 생성 요청이 있을 때") {
        When("정산을 생성하면") {
            Then("0원 PENDING 정산을 저장한다") {
                val month = YearMonth.of(2025, 3)
                val input = CreationSettlementDto(creatorId = 3L, month = month)
                val savedSettlement = slot<Settlement>()

                every {
                    settlementRepository.existsByCreatorIdAndSettlementMonth(3L, month.atDay(1))
                } returns false
                every { settlementCalculationService.calculate(month, 3L) } returns emptyList()
                every { settlementRepository.saveAndFlush(capture(savedSettlement)) } answers { savedSettlement.captured }

                val result = settlementService.create(input)

                savedSettlement.captured.totalSaleAmount shouldBe 0L
                savedSettlement.captured.totalCancelAmount shouldBe 0L
                savedSettlement.captured.netSalesAmount shouldBe 0L
                savedSettlement.captured.commissionAmount shouldBe 0L
                savedSettlement.captured.settlementAmount shouldBe 0L
                savedSettlement.captured.saleCount shouldBe 0L
                savedSettlement.captured.cancelCount shouldBe 0L

                result.settlementAmount shouldBe 0L
                result.saleCount shouldBe 0L
                result.cancelCount shouldBe 0L
            }
        }
    }

    Given("마감되지 않은 월 정산 생성 요청이 있을 때") {
        val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

        When("현재월 정산을 생성하면") {
            Then("마감되지 않은 월 오류를 반환하고 계산과 저장을 하지 않는다") {
                val exception = shouldThrow<ApiException> {
                    settlementService.create(CreationSettlementDto(creatorId = 1L, month = currentMonth))
                }

                exception.errorCode shouldBe ErrorCode.INVALID_SETTLEMENT_MONTH_NOT_CLOSED

                verify(exactly = 0) {
                    settlementRepository.existsByCreatorIdAndSettlementMonth(any(), any())
                    settlementCalculationService.calculate(any(), any())
                    settlementRepository.saveAndFlush(any<Settlement>())
                }
            }
        }

        When("미래월 정산을 생성하면") {
            Then("마감되지 않은 월 오류를 반환하고 계산과 저장을 하지 않는다") {
                val exception = shouldThrow<ApiException> {
                    settlementService.create(CreationSettlementDto(creatorId = 1L, month = currentMonth.plusMonths(1)))
                }

                exception.errorCode shouldBe ErrorCode.INVALID_SETTLEMENT_MONTH_NOT_CLOSED

                verify(exactly = 0) {
                    settlementRepository.existsByCreatorIdAndSettlementMonth(any(), any())
                    settlementCalculationService.calculate(any(), any())
                    settlementRepository.saveAndFlush(any<Settlement>())
                }
            }
        }
    }

    Given("이미 같은 creator와 month의 정산이 있을 때") {
        When("정산을 생성하면") {
            Then("중복 정산 오류를 반환하고 계산과 저장을 하지 않는다") {
                val month = YearMonth.of(2025, 3)

                every {
                    settlementRepository.existsByCreatorIdAndSettlementMonth(1L, month.atDay(1))
                } returns true

                val exception = shouldThrow<ApiException> {
                    settlementService.create(CreationSettlementDto(creatorId = 1L, month = month))
                }

                exception.errorCode shouldBe ErrorCode.DUPLICATE_SETTLEMENT

                verify(exactly = 0) {
                    settlementCalculationService.calculate(any(), any())
                    settlementRepository.saveAndFlush(any<Settlement>())
                }
            }
        }
    }

    Given("저장 시점에 unique 충돌이 발생하는 정산 생성 요청이 있을 때") {
        When("정산을 생성하면") {
            Then("중복 정산 오류로 변환한다") {
                val month = YearMonth.of(2025, 3)
                val input = CreationSettlementDto(creatorId = 1L, month = month)

                every {
                    settlementRepository.existsByCreatorIdAndSettlementMonth(1L, month.atDay(1))
                } returns false
                every { settlementCalculationService.calculate(month, 1L) } returns listOf(predictedSettlement(month))
                every { settlementRepository.findAmountByCreatorIdAndSettlementMonth(1L, month) } returns null
                every {
                    settlementRepository.saveAndFlush(any<Settlement>())
                } throws DataIntegrityViolationException("duplicate settlement")

                val exception = shouldThrow<ApiException> {
                    settlementService.create(input)
                }

                exception.errorCode shouldBe ErrorCode.DUPLICATE_SETTLEMENT
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
