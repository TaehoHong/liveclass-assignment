package com.futureschole.liveclass.domain.sale_record.service

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.course.service.CourseService
import com.futureschole.liveclass.domain.sale_record.dto.CreationSaleRecordDto
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SaleRecordService(
    private val courseService: CourseService,
    private val saleRecordRepository: SaleRecordRepository
) {

    @Transactional
    fun save(input: CreationSaleRecordDto): SaleRecord {
        return SaleRecord(
            courseId = this.courseService.getById(input.courseId).id,
            studentId = input.studentId,
            amount = input.amount,
            paidAt = input.paidAt
        ).let {
            saleRecordRepository.save(it)
        }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): SaleRecord {
        return this.saleRecordRepository.findById(id)
            .orElseThrow { ApiException(ErrorCode.NOT_FOUND_SALE_RECORD) }
    }
}