package com.futureschole.liveclass.unit.repository.cancel_record

import com.futureschole.liveclass.domain.cancel_record.repository.CancelRecordRepository
import com.futureschole.liveclass.integration.BaseIntegrationTest
import com.futureschole.liveclass.testdata.TestDataInserter
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.YearMonth

class CancelRecordRepositoryTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var cancelRecordRepository: CancelRecordRepository

    @Autowired
    private lateinit var testDataInserter: TestDataInserter

    init {
        Given("취소 내역 집계용 데이터가 준비되어 있을 때") {
            When("creatorId 없이 월별 취소 내역을 조회하면") {
                Then("월 경계를 적용하고 creator별 map으로 반환한다") {
                    testDataInserter.prepareCancelRecords()

                    val result = cancelRecordRepository.findCreatorIdToCancelRecords(
                        creatorId = null,
                        month = YearMonth.of(2025, 3)
                    )

                    result.keys shouldBe setOf(1L, 2L)
                    result.getValue(1L).map { it.id } shouldBe listOf(401L, 402L)
                    result.getValue(2L).map { it.id } shouldBe listOf(404L)
                }
            }

            When("creatorId와 월을 함께 지정해 취소 내역을 조회하면") {
                Then("월 경계 안의 해당 creator 취소 내역만 반환한다") {
                    testDataInserter.prepareCancelRecords()

                    val result = cancelRecordRepository.findCreatorIdToCancelRecords(
                        creatorId = 1L,
                        month = YearMonth.of(2025, 3)
                    )

                    result.keys shouldBe setOf(1L)
                    result.getValue(1L).map { it.id } shouldBe listOf(401L, 402L)
                }
            }
        }
    }
}
