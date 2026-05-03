package com.futureschole.liveclass.unit.repository.settlement

import com.futureschole.liveclass.domain.settlement.repository.SettlementRepository
import com.futureschole.liveclass.integration.BaseIntegrationTest
import com.futureschole.liveclass.testdata.TestDataInserter
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
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

                    result.map { it.id } shouldBe listOf(105L, 104L, 103L, 106L, 102L, 101L)
                }
            }

            When("creatorId와 settlementMonth를 함께 지정해 조회하면") {
                Then("두 조건을 모두 만족하는 정산만 반환한다") {
                    testDataInserter.prepareSettlements()

                    val result = settlementRepository.findAll(
                        creatorId = 1L,
                        settlementMonth = YearMonth.of(2025, 3)
                    )

                    result.map { it.id } shouldBe listOf(102L, 101L)
                }
            }
        }
    }
}
