package com.futureschole.liveclass.domain.sale_record.controller

import com.futureschole.liveclass.domain.sale_record.dto.CreationSaleRecordDto
import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordDto
import com.futureschole.liveclass.domain.sale_record.service.SaleRecordService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/sale-record")
@RestController
class SaleRecordController(
    private val saleRecordService: SaleRecordService
) {

    @PostMapping
    fun save(@Valid @RequestBody input: CreationSaleRecordDto): SaleRecordDto {
        return saleRecordService.save(input)
            .let {
                SaleRecordDto(
                    it.id,
                    it.courseId,
                    it.studentId,
                    it.amount,
                    it.paidAt,
                    it.createdAt
                )
            }
    }
}