package com.futureschole.liveclass.domain.cancel_record.dto

import java.time.LocalDateTime

class CancelRecordDto(
    val id: Long,
    val saleRecordId: Long,
    val amount: Long,
    val cancelAt: LocalDateTime,
    val createdAt: LocalDateTime
)
