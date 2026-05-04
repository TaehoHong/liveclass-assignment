package com.futureschole.liveclass.unit.repository.settlement

import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import com.futureschole.liveclass.integration.BaseIntegrationTest
import com.futureschole.liveclass.testdata.TestDataInserter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class SettlementRepositoryTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var settlementRepository: SettlementRepository

    @Autowired
    private lateinit var testDataInserter: TestDataInserter

    init {
        Given("정산 검색용 데이터가 준비되어 있을 때") {
            When("필터 없이 조회하면") {
                Then("전체 정산을 settlementMonth desc, creatorId asc, createdAt desc, id desc 순서로 반환한다") {
                    testDataInserter.prepareSettlements()

                    val result = settlementRepository.findAll(
                        creatorId = null,
                        settlementMonth = null
                    )

                    result.map { it.id } shouldBe listOf(103L, 104L, 105L, 102L, 106L, 101L)
                }
            }

            When("creatorId와 settlementMonth를 함께 지정해 조회하면") {
                Then("두 조건을 모두 만족하는 정산만 반환한다") {
                    testDataInserter.prepareSettlements()

                    val result = settlementRepository.findAll(
                        creatorId = 1L,
                        settlementMonth = YearMonth.of(2025, 3)
                    )

                    result.map { it.id } shouldBe listOf(102L)
                }
            }
        }

        Given("같은 creator와 settlementMonth의 정산이 이미 저장되어 있을 때") {
            When("동일한 creator와 settlementMonth의 정산을 다시 저장하면") {
                Then("DB unique 제약 위반이 발생한다") {
                    val settlementMonth = LocalDate.of(2025, 3, 1)

                    settlementRepository.saveAndFlush(
                        settlement(creatorId = 1L, settlementMonth = settlementMonth)
                    )

                    shouldThrow<DataIntegrityViolationException> {
                        settlementRepository.saveAndFlush(
                            settlement(creatorId = 1L, settlementMonth = settlementMonth)
                        )
                    }
                }
            }
        }
    }
}

private fun settlement(
    creatorId: Long,
    settlementMonth: LocalDate,
): Settlement {
    return Settlement(
        id = 0L,
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
