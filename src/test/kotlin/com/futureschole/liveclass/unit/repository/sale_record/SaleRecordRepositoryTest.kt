package com.futureschole.liveclass.unit.repository.sale_record

import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import com.futureschole.liveclass.integration.BaseIntegrationTest
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.LocalDateTime

class SaleRecordRepositoryTest: BaseIntegrationTest() {
    @Autowired
    private lateinit var saleRecordRepository: SaleRecordRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    init {
        Given("판매 내역 검색용 데이터가 준비되어 있을 때") {
            When("필터 없이 조회하면") {
                Then("전체 판매 내역을 projection 형태로 paidAt desc, id desc 순서로 반환한다") {
                    prepareData()

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
                    prepareData()

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
                    prepareData()

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
                    prepareData()

                    val result = saleRecordRepository.findAllByCreatorAndPaidAtRange(
                        creatorId = 1L,
                        startPaidAt = null,
                        endPaidAt = LocalDateTime.of(2025, 4, 1, 0, 0)
                    )

                    result.map { it.id } shouldBe listOf(104L, 103L, 102L, 101L)
                }
            }
        }
    }

    private fun prepareData() {
        saveSaleRecord(101L, 1L, 900001L, 30000L, LocalDateTime.of(2025, 2, 28, 23, 59))
        saveSaleRecord(102L, 1L, 900002L, 30000L, LocalDateTime.of(2025, 3, 1, 0, 0))
        saveSaleRecord(103L, 1L, 900003L, 40000L, LocalDateTime.of(2025, 3, 15, 12, 0))
        saveSaleRecord(104L, 1L, 900004L, 50000L, LocalDateTime.of(2025, 3, 31, 23, 59))
        saveSaleRecord(105L, 1L, 900005L, 50000L, LocalDateTime.of(2025, 4, 1, 0, 0))
        saveSaleRecord(106L, 3L, 900006L, 60000L, LocalDateTime.of(2025, 4, 1, 0, 0))
        saveSaleRecord(107L, 3L, 900007L, 60000L, LocalDateTime.of(2025, 3, 20, 12, 0))
    }

    private fun saveSaleRecord(
        id: Long,
        courseId: Long,
        studentId: Long,
        amount: Long,
        paidAt: LocalDateTime
    ) {
        jdbcTemplate.update(
            """
                INSERT INTO sale_record (id, course_id, student_id, amount, paid_at)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            courseId,
            studentId,
            amount,
            Timestamp.valueOf(paidAt)
        )
    }
}
