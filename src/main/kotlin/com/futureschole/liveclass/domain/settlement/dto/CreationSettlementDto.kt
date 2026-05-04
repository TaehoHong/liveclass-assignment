package com.futureschole.liveclass.domain.settlement.dto

import jakarta.validation.constraints.Min
import java.time.YearMonth

data class CreationSettlementDto(

    @field:Min(1)
    val creatorId: Long,

    val month: YearMonth
)
