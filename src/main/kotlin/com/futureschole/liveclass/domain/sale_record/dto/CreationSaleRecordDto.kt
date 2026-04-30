package com.futureschole.liveclass.domain.sale_record.dto

import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

data class CreationSaleRecordDto(

    @field:NotNull
    @field:Min(1)
    val courseId: Long,

    @field:NotNull
    @field:Min(1)
    val studentId: Long,

    @field:NotNull
    @field:Min(0)
    val amount: Long,

    @field:NotNull
    val paidAt: LocalDateTime,
)