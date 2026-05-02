package com.futureschole.liveclass.domain.sale_record.dto

import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import java.time.LocalDateTime

data class SaleRecordDto(
    val id: Long,
    val courseId: Long,
    val studentId: Long,
    val amount: Long,
    val paidAt: LocalDateTime,
    val createdAt: LocalDateTime
) {
    constructor(saleRecord: SaleRecord) : this(
        id = saleRecord.id,
        courseId = saleRecord.course.id,
        studentId = saleRecord.studentId,
        amount = saleRecord.amount,
        paidAt = saleRecord.paidAt,
        createdAt = saleRecord.createdAt
    )
}
