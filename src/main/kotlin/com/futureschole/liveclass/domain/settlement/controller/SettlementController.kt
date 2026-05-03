package com.futureschole.liveclass.domain.settlement.controller

import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.domain.settlement.service.SettlementService
import com.futureschole.liveclass.security.withOwnedCreatorOrAdmin
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.YearMonth

@RequestMapping("/api/settlement")
@RestController
class SettlementController(
    private val settlementService: SettlementService
) {

    @GetMapping
    fun findAll(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") month: YearMonth?,
        @RequestParam(required = false) creatorId: Long?,
    ): List<SettlementMonthlyResponseDto> {
        return withOwnedCreatorOrAdmin(creatorId) { scopedCreatorId ->
            settlementService.findAll(month, scopedCreatorId)
        }
    }
}
