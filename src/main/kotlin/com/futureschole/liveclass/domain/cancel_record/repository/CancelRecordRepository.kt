package com.futureschole.liveclass.domain.cancel_record.repository

import com.futureschole.liveclass.domain.cancel_record.entity.CancelRecord
import org.springframework.data.jpa.repository.JpaRepository

interface CancelRecordRepository: JpaRepository<CancelRecord, Long> {
    fun findAllBySaleRecordId(saleRecordId: Long): List<CancelRecord>
}
