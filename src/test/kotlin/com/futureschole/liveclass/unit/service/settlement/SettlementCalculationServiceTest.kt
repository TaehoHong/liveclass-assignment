package com.futureschole.liveclass.unit.service.settlement

import com.futureschole.liveclass.common.entity.Creator
import com.futureschole.liveclass.domain.cancel_record.entity.CancelRecord
import com.futureschole.liveclass.domain.cancel_record.repository.CancelRecordRepository
import com.futureschole.liveclass.domain.course.entity.Course
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import com.futureschole.liveclass.domain.settlement.service.SettlementCalculationService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.time.YearMonth

class SettlementCalculationServiceTest: BehaviorSpec({
    val saleRecordRepository = mockk<SaleRecordRepository>()
    val cancelRecordRepository = mockk<CancelRecordRepository>()
    val settlementCalculationService = SettlementCalculationService(saleRecordRepository, cancelRecordRepository)

    afterEach { clearAllMocks() }

    Given("판매와 취소 내역이 함께 있을 때") {
        When("정산을 계산하면") {
            Then("순매출, 정수 수수료, 정산 금액을 계산한다") {
                val month = YearMonth.of(2025, 3)
                val saleRecord = createSaleRecord()
                val cancelRecord = cancelRecord()

                every { saleRecordRepository.findCreatorIdToSaleRecords(1L, month) } returns mapOf(
                    saleRecord.course.creator.id to listOf(saleRecord)
                )
                every { cancelRecordRepository.findCreatorIdToCancelRecords(1L, month) } returns mapOf(
                    1L to listOf(cancelRecord)
                )

                val result = settlementCalculationService.calculate(month, creatorId = 1L)

                result.size shouldBe 1

                val settlement = result[0]
                settlement.creatorId shouldBe 1L
                settlement.settlementMonth shouldBe month.atDay(1)
                settlement.totalSaleAmount shouldBe 151L
                settlement.totalCancelAmount shouldBe 50L
                settlement.netSalesAmount shouldBe 101L
                settlement.commissionRate shouldBe 20.toShort()
                settlement.commissionAmount shouldBe 20L
                settlement.settlementAmount shouldBe 81L
                settlement.saleCount shouldBe 1L
                settlement.cancelCount shouldBe 1L
                settlement.settlementId shouldBe null
                settlement.status shouldBe null
            }
        }
    }
})

private fun createSaleRecord(): SaleRecord {
    return SaleRecord(
        id = 1L,
        course = Course(
            id = 1L,
            creator = Creator(id = 1L, name = "creator"),
            title = "course"
        ),
        studentId = 900000L,
        amount = 151,
        paidAt = LocalDateTime.of(2025, 3, 1, 10, 0)
    )
}

private fun cancelRecord(): CancelRecord {
    return CancelRecord(
        id = 1L,
        saleRecordId = 1L,
        amount = 50L,
        cancelAt = LocalDateTime.of(2025, 3, 1, 11, 0)
    )
}
