package com.futureschole.liveclass.domain.sale_record.controller

import com.futureschole.liveclass.domain.sale_record.dto.CreationSaleRecordDto
import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordDto
import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordSearchDto
import com.futureschole.liveclass.domain.sale_record.service.SaleRecordService
import com.futureschole.liveclass.security.withOwnedCreatorOrAdmin
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("/api/sale-record")
@RestController
class SaleRecordController(
    private val saleRecordService: SaleRecordService
) {

    @PostMapping
    fun save(@Valid @RequestBody input: CreationSaleRecordDto): SaleRecordDto {
        return SaleRecordDto(saleRecordService.save(input))
    }

    @GetMapping
    fun findAll(
        @RequestParam(required = false) creatorId: Long?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
    ): List<SaleRecordSearchDto> {
        return withOwnedCreatorOrAdmin(creatorId) { scopedCreatorId ->
            saleRecordService.findAll(scopedCreatorId, startDate, endDate)
        }
    }
}
