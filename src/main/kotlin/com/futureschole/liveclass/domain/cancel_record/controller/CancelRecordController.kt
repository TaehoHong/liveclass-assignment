package com.futureschole.liveclass.domain.cancel_record.controller

import com.futureschole.liveclass.domain.cancel_record.dto.CancelRecordDto
import com.futureschole.liveclass.domain.cancel_record.dto.CreationCancelRecordDto
import com.futureschole.liveclass.domain.cancel_record.service.CancelRecordService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/cancel-record")
@RestController
class CancelRecordController(
    private val cancelRecordService: CancelRecordService
) {

    @PostMapping
    fun save(@Valid @RequestBody input: CreationCancelRecordDto): CancelRecordDto {
        return cancelRecordService.save(input)
            .let {
                CancelRecordDto(
                    it.id,
                    it.saleRecordId,
                    it.amount,
                    it.cancelAt,
                    it.createdAt
                )
            }
    }
}
