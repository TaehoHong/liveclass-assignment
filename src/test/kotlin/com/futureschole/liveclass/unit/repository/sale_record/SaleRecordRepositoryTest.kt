package com.futureschole.liveclass.unit.repository.sale_record

import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import com.futureschole.liveclass.integration.BaseIntegrationTest
import com.futureschole.liveclass.testdata.TestDataInserter
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.YearMonth

class SaleRecordRepositoryTest: BaseIntegrationTest() {
    @Autowired
    private lateinit var saleRecordRepository: SaleRecordRepository

    @Autowired
    private lateinit var testDataInserter: TestDataInserter

    init {
        Given("판매 내역 검색용 데이터가 준비되어 있을 때") {
            When("필터 없이 조회하면") {
                Then("전체 판매 내역을 projection 형태로 paidAt desc, id desc 순서로 반환한다") {
                    testDataInserter.prepareSaleRecords()

                    val result = saleRecordRepository.findAllByCreatorAndPaidAtRange(
                        creatorId = null,
                        startPaidAt = null,
                        endPaidAt = null
                    )

                    result.map { it.id } shouldBe listOf(106L, 105L, 104L, 107L, 103L, 102L, 101L)

                    result[0].course.id shouldBe 3L
                    result[0].course.title shouldBe "Kotlin 기초"
                    result[0].course.creator.id shouldBe 2L
                    result[0].course.creator.name shouldBe "이강사"
                }
            }

            When("creatorId와 paidAt 범위로 조회하면") {
                Then("해당 creator의 범위 내 판매 내역만 반환한다") {
                    testDataInserter.prepareSaleRecords()

                    val result = saleRecordRepository.findAllByCreatorAndPaidAtRange(
                        creatorId = 1L,
                        startPaidAt = LocalDateTime.of(2025, 3, 1, 0, 0),
                        endPaidAt = LocalDateTime.of(2025, 4, 1, 0, 0)
                    )

                    result.map { it.id } shouldBe listOf(104L, 103L, 102L)
                }
            }

            When("startPaidAt만 지정해 조회하면") {
                Then("하한만 적용한다") {
                    testDataInserter.prepareSaleRecords()

                    val result = saleRecordRepository.findAllByCreatorAndPaidAtRange(
                        creatorId = 1L,
                        startPaidAt = LocalDateTime.of(2025, 3, 1, 0, 0),
                        endPaidAt = null
                    )

                    result.map { it.id } shouldBe listOf(105L, 104L, 103L, 102L)
                }
            }

            When("endPaidAt만 지정해 조회하면") {
                Then("exclusive 상한만 적용한다") {
                    testDataInserter.prepareSaleRecords()

                    val result = saleRecordRepository.findAllByCreatorAndPaidAtRange(
                        creatorId = 1L,
                        startPaidAt = null,
                        endPaidAt = LocalDateTime.of(2025, 4, 1, 0, 0)
                    )

                    result.map { it.id } shouldBe listOf(104L, 103L, 102L, 101L)
                }
            }
        }

        Given("판매 내역 집계용 데이터가 준비되어 있을 때") {
            When("creatorId 없이 월별 판매 내역을 조회하면") {
                Then("월 경계를 적용하고 creator별 map으로 반환한다") {
                    testDataInserter.prepareSaleRecords()

                    val result = saleRecordRepository.findCreatorIdToSaleRecords(
                        creatorId = null,
                        month = YearMonth.of(2025, 3)
                    )

                    result.keys shouldBe setOf(1L, 2L)
                    result.getValue(1L).map { it.id } shouldBe listOf(102L, 103L, 104L)
                    result.getValue(2L).map { it.id } shouldBe listOf(107L)
                }
            }

            When("creatorId와 월을 함께 지정해 판매 내역을 조회하면") {
                Then("월 경계 안의 해당 creator 판매 내역만 반환한다") {
                    testDataInserter.prepareSaleRecords()

                    val result = saleRecordRepository.findCreatorIdToSaleRecords(
                        creatorId = 1L,
                        month = YearMonth.of(2025, 3)
                    )

                    result.keys shouldBe setOf(1L)
                    result.getValue(1L).map { it.id } shouldBe listOf(102L, 103L, 104L)
                }
            }
        }
    }
}
