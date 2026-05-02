package com.futureschole.liveclass.domain.sale_record.service

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.course.service.CourseService
import com.futureschole.liveclass.domain.sale_record.dto.CreationSaleRecordDto
import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordSearchDto
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SaleRecordService(
    private val courseService: CourseService,
    private val saleRecordRepository: SaleRecordRepository
) {

    @Transactional
    fun save(input: CreationSaleRecordDto): SaleRecord {
        val course = courseService.getById(input.courseId)

        return SaleRecord(
            course = course,
            studentId = input.studentId,
            amount = input.amount,
            paidAt = input.paidAt
        ).let(saleRecordRepository::save)
    }

    @Transactional(readOnly = true)
    fun findAll(creatorId: Long?, startDate: LocalDate?, endDate: LocalDate?): List<SaleRecordSearchDto> {
        validateDateRange(startDate, endDate)

        return saleRecordRepository.findAllByCreatorAndPaidAtRange(
            creatorId = creatorId,
            startPaidAt = startDate?.atStartOfDay(),
            endPaidAt = endDate?.plusDays(1)?.atStartOfDay()
        )
    }

    private fun validateDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw ApiException(ErrorCode.INVALID_DATE_RANGE)
        }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): SaleRecord {
        return this.saleRecordRepository.findById(id)
            .orElseThrow { ApiException(ErrorCode.NOT_FOUND_SALE_RECORD) }
    }
}
