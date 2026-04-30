package com.futureschole.liveclass.domain.sale_record.dto

import jakarta.validation.constraints.Min
import java.time.LocalDateTime

data class CreationSaleRecordDto(

    @field:Min(1)
    val courseId: Long,

    @field:Min(1)
    val studentId: Long,

    @field:Min(0)
    val amount: Long,

    val paidAt: LocalDateTime,
)