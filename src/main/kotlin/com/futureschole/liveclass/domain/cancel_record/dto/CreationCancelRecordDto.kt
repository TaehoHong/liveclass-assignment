package com.futureschole.liveclass.domain.cancel_record.dto

import jakarta.validation.constraints.Min
import java.time.LocalDateTime

data class CreationCancelRecordDto(

    @field:Min(1)
    val saleRecordId: Long,

    @field:Min(1)
    val amount: Long,

    val cancelAt: LocalDateTime,
)
