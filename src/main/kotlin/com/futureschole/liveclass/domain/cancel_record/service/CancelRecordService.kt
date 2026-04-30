package com.futureschole.liveclass.domain.cancel_record.service

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.cancel_record.dto.CreationCancelRecordDto
import com.futureschole.liveclass.domain.cancel_record.entity.CancelRecord
import com.futureschole.liveclass.domain.cancel_record.repository.CancelRecordRepository
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.service.SaleRecordService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CancelRecordService(
    private val saleRecordService: SaleRecordService,
    private val cancelRecordRepository: CancelRecordRepository
) {

    @Transactional
    fun save(input: CreationCancelRecordDto): CancelRecord {
        val saleRecord = saleRecordService.getById(input.saleRecordId)

        validateCancelAt(input.cancelAt, saleRecord.paidAt)
        validateRefundAmount(saleRecord, input.amount)

        return CancelRecord(
            saleRecordId = saleRecord.id,
            amount = input.amount,
            cancelAt = input.cancelAt
        ).let {
            cancelRecordRepository.save(it)
        }
    }

    private fun validateCancelAt(cancelAt: LocalDateTime, paidAt: LocalDateTime) {
        if(!cancelAt.isAfter(paidAt)) {
            throw ApiException(ErrorCode.INVALID_CANCEL_AT_NOT_AFTER_PAID_AT)
        }

        if(cancelAt.isAfter(LocalDateTime.now())) {
            throw ApiException(ErrorCode.INVALID_CANCEL_AT_IN_FUTURE)
        }
    }

    private fun validateRefundAmount(saleRecord: SaleRecord, inputAmount: Long) {
        cancelRecordRepository.findAllBySaleRecordId(saleRecord.id)
            .sumOf { it.amount }
            .apply {
                val remainingAmount = saleRecord.amount - this
                if (inputAmount > remainingAmount) {
                    throw ApiException(ErrorCode.INVALID_CANCEL_AMOUNT)
                }
            }
    }
 }
