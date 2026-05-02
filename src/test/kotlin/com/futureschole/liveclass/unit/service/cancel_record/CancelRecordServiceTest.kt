package com.futureschole.liveclass.unit.service.cancel_record

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.common.entity.Creator
import com.futureschole.liveclass.domain.cancel_record.dto.CreationCancelRecordDto
import com.futureschole.liveclass.domain.cancel_record.entity.CancelRecord
import com.futureschole.liveclass.domain.cancel_record.repository.CancelRecordRepository
import com.futureschole.liveclass.domain.cancel_record.service.CancelRecordService
import com.futureschole.liveclass.domain.course.entity.Course
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.service.SaleRecordService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class CancelRecordServiceTest: BehaviorSpec({
    val saleRecordService = mockk<SaleRecordService>()
    val cancelRecordRepository = mockk<CancelRecordRepository>()
    val cancelRecordService = CancelRecordService(saleRecordService, cancelRecordRepository)

    afterEach { clearAllMocks() }

    Given("존재하는 판매 내역이 있을 때") {
        val saleRecord = SaleRecord(
            id = 1L,
            course = Course(
                id = 1L,
                creator = Creator(id = 1L, name = "김강사"),
                title = "Spring Boot 입문"
            ),
            studentId = 900001L,
            amount = 50000L,
            paidAt = LocalDateTime.now().minusDays(2)
        )

        When("취소 내역을 저장하면") {
            val input = CreationCancelRecordDto(
                saleRecordId = saleRecord.id,
                amount = 20000L,
                cancelAt = saleRecord.paidAt.plusDays(1)
            )

            every { saleRecordService.getById(input.saleRecordId) } returns saleRecord
            every { cancelRecordRepository.findAllBySaleRecordId(saleRecord.id) } returns emptyList()
            every { cancelRecordRepository.save(any<CancelRecord>()) } answers { firstArg() }

            val savedCancelRecord = cancelRecordService.save(input)

            Then("판매 내역을 조회한 뒤 입력값이 반영된 취소 내역을 저장한다") {
                savedCancelRecord.saleRecordId shouldBe input.saleRecordId
                savedCancelRecord.amount shouldBe input.amount
                savedCancelRecord.cancelAt shouldBe input.cancelAt
            }
        }

        When("취소 일시가 결제 일시와 같은 취소 내역을 저장하면") {
            val input = CreationCancelRecordDto(
                saleRecordId = saleRecord.id,
                amount = 20000L,
                cancelAt = saleRecord.paidAt
            )

            every { saleRecordService.getById(input.saleRecordId) } returns saleRecord

            val exception = shouldThrow<ApiException> {
                cancelRecordService.save(input)
            }

            Then("취소 일시 검증 예외를 발생시키고 취소 내역은 저장하지 않는다") {
                exception.errorCode shouldBe ErrorCode.INVALID_CANCEL_AT_NOT_AFTER_PAID_AT

                verify(exactly = 0) { cancelRecordRepository.save(any<CancelRecord>()) }
            }
        }

        When("취소 일시가 결제 일시보다 이전인 취소 내역을 저장하면") {
            val input = CreationCancelRecordDto(
                saleRecordId = saleRecord.id,
                amount = 20000L,
                cancelAt = saleRecord.paidAt.minusSeconds(1)
            )

            every { saleRecordService.getById(input.saleRecordId) } returns saleRecord

            val exception = shouldThrow<ApiException> {
                cancelRecordService.save(input)
            }

            Then("취소 일시 검증 예외를 발생시키고 취소 내역은 저장하지 않는다") {
                exception.errorCode shouldBe ErrorCode.INVALID_CANCEL_AT_NOT_AFTER_PAID_AT

                verify(exactly = 0) { cancelRecordRepository.save(any<CancelRecord>()) }
            }
        }

        When("취소 일시가 미래인 취소 내역을 저장하면") {
            val input = CreationCancelRecordDto(
                saleRecordId = saleRecord.id,
                amount = 20000L,
                cancelAt = LocalDateTime.now().plusDays(1)
            )

            every { saleRecordService.getById(input.saleRecordId) } returns saleRecord

            val exception = shouldThrow<ApiException> {
                cancelRecordService.save(input)
            }

            Then("미래 취소 일시 검증 예외를 발생시키고 취소 내역은 저장하지 않는다") {
                exception.errorCode shouldBe ErrorCode.INVALID_CANCEL_AT_IN_FUTURE

                verify(exactly = 0) { cancelRecordRepository.save(any<CancelRecord>()) }
            }
        }

        When("기존 취소 없이 요청 금액이 판매 금액을 초과하는 취소 내역을 저장하면") {
            val input = CreationCancelRecordDto(
                saleRecordId = saleRecord.id,
                amount = 50001L,
                cancelAt = saleRecord.paidAt.plusDays(1)
            )

            every { saleRecordService.getById(input.saleRecordId) } returns saleRecord
            every { cancelRecordRepository.findAllBySaleRecordId(saleRecord.id) } returns emptyList()

            val exception = shouldThrow<ApiException> {
                cancelRecordService.save(input)
            }

            Then("취소 금액 검증 예외를 발생시키고 취소 내역은 저장하지 않는다") {
                exception.errorCode shouldBe ErrorCode.INVALID_CANCEL_AMOUNT

                verify(exactly = 0) { cancelRecordRepository.save(any<CancelRecord>()) }
            }
        }
    }

    Given("기존 취소 내역이 있는 판매 내역이 있을 때") {
        val saleRecord = SaleRecord(
            id = 1L,
            course = Course(
                id = 1L,
                creator = Creator(id = 1L, name = "김강사"),
                title = "Spring Boot 입문"
            ),
            studentId = 900001L,
            amount = 50000L,
            paidAt = LocalDateTime.now().minusDays(2)
        )
        val cancelRecord = CancelRecord(
            id = 1L,
            saleRecordId = saleRecord.id,
            amount = 30000L,
            cancelAt = saleRecord.paidAt.plusHours(1)
        )

        When("잔여 금액과 같은 취소 내역을 저장하면") {
            val input = CreationCancelRecordDto(
                saleRecordId = saleRecord.id,
                amount = 20000L,
                cancelAt = saleRecord.paidAt.plusDays(1)
            )

            every { saleRecordService.getById(input.saleRecordId) } returns saleRecord
            every { cancelRecordRepository.findAllBySaleRecordId(saleRecord.id) } returns listOf(cancelRecord)
            every { cancelRecordRepository.save(any<CancelRecord>()) } answers { firstArg() }

            val savedCancelRecord = cancelRecordService.save(input)

            Then("잔여 금액까지 취소 내역을 저장한다") {
                savedCancelRecord.saleRecordId shouldBe input.saleRecordId
                savedCancelRecord.amount shouldBe input.amount
                savedCancelRecord.cancelAt shouldBe input.cancelAt
            }
        }

        When("잔여 금액을 초과하는 취소 내역을 저장하면") {
            val input = CreationCancelRecordDto(
                saleRecordId = saleRecord.id,
                amount = 20001L,
                cancelAt = saleRecord.paidAt.plusDays(1)
            )

            every { saleRecordService.getById(input.saleRecordId) } returns saleRecord
            every { cancelRecordRepository.findAllBySaleRecordId(saleRecord.id) } returns listOf(cancelRecord)

            val exception = shouldThrow<ApiException> {
                cancelRecordService.save(input)
            }

            Then("취소 금액 검증 예외를 발생시키고 취소 내역은 저장하지 않는다") {
                exception.errorCode shouldBe ErrorCode.INVALID_CANCEL_AMOUNT

                verify(exactly = 0) { cancelRecordRepository.save(any<CancelRecord>()) }
            }
        }
    }

    Given("존재하지 않는 판매 내역의 취소 내역 생성 요청이 있을 때") {
        val input = CreationCancelRecordDto(
            saleRecordId = 999999L,
            amount = 20000L,
            cancelAt = LocalDateTime.now().minusDays(1)
        )

        When("취소 내역을 저장하면") {
            every { saleRecordService.getById(input.saleRecordId) } throws ApiException(ErrorCode.NOT_FOUND_SALE_RECORD)

            val exception = shouldThrow<ApiException> {
                cancelRecordService.save(input)
            }

            Then("판매 내역 없음 예외를 전파하고 취소 내역은 저장하지 않는다") {
                exception.errorCode shouldBe ErrorCode.NOT_FOUND_SALE_RECORD

                verify(exactly = 0) { cancelRecordRepository.save(any<CancelRecord>()) }
            }
        }
    }
})
