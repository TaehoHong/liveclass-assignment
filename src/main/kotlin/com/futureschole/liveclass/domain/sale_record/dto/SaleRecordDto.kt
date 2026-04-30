package com.futureschole.liveclass.domain.sale_record.dto

import java.time.LocalDateTime

class SaleRecordDto(
    val id: Long,
    val courseId: Long,
    val studentId: Long,
    val amount: Long,
    val paidAt: LocalDateTime,
    val createdAt: LocalDateTime
)