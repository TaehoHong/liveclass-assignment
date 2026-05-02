package com.futureschole.liveclass.unit.service.sale_record

import com.futureschole.liveclass.common.entity.Creator
import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.course.entity.Course
import com.futureschole.liveclass.domain.course.service.CourseService
import com.futureschole.liveclass.domain.sale_record.dto.CreationSaleRecordDto
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import com.futureschole.liveclass.domain.sale_record.service.SaleRecordService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime

class SaleRecordServiceTest: BehaviorSpec({
    val courseService = mockk<CourseService>()
    val saleRecordRepository = mockk<SaleRecordRepository>()
    val saleRecordService = SaleRecordService(courseService, saleRecordRepository)

    afterEach { clearAllMocks() }

    Given("존재하는 강의의 판매 내역 생성 요청이 있을 때") {
        val input = CreationSaleRecordDto(
            courseId = 1L,
            studentId = 900001L,
            amount = 30000L,
            paidAt = LocalDateTime.of(2026, 4, 30, 10, 15, 0)
        )

        every { courseService.getById(input.courseId) } returns Course(
            id = input.courseId,
            creator = Creator(id = 1L, name = "김강사"),
            title = "Spring Boot 입문"
        )
        every { saleRecordRepository.save(any<SaleRecord>()) } answers { firstArg() }

        When("판매 내역을 저장하면") {
            val savedSaleRecord = saleRecordService.save(input)

            Then("강의를 조회한 뒤 입력값이 반영된 판매 내역을 저장한다") {
                savedSaleRecord.course.id shouldBe input.courseId
                savedSaleRecord.studentId shouldBe input.studentId
                savedSaleRecord.amount shouldBe input.amount
                savedSaleRecord.paidAt shouldBe input.paidAt
            }
        }
    }

    Given("존재하지 않는 강의의 판매 내역 생성 요청이 있을 때") {

        val input = CreationSaleRecordDto(
            courseId = 999999L,
            studentId = 900002L,
            amount = 30000L,
            paidAt = LocalDateTime.of(2026, 4, 30, 10, 15, 0)
        )

        every { courseService.getById(input.courseId) } throws ApiException(ErrorCode.NOT_FOUND_COURSE)

        When("판매 내역을 저장하면") {
            val exception = shouldThrow<ApiException> {
                saleRecordService.save(input)
            }

            Then("강의 없음 예외를 전파하고 판매 내역은 저장하지 않는다") {
                exception.errorCode shouldBe ErrorCode.NOT_FOUND_COURSE

                verify(exactly = 0) { saleRecordRepository.save(any<SaleRecord>()) }
            }
        }
    }

    Given("시작일이 종료일보다 늦은 판매 내역 조회 요청이 있을 때") {
        When("판매 내역을 조회하면") {
            val exception = shouldThrow<ApiException> {
                saleRecordService.findAll(
                    creatorId = 1L,
                    startDate = LocalDate.of(2025, 4, 1),
                    endDate = LocalDate.of(2025, 3, 31)
                )
            }

            Then("날짜 범위 오류를 반환하고 repository를 호출하지 않는다") {
                exception.errorCode shouldBe ErrorCode.INVALID_DATE_RANGE

                verify(exactly = 0) {
                    saleRecordRepository.findAllByCreatorAndPaidAtRange(any(), any(), any())
                }
            }
        }
    }
})
