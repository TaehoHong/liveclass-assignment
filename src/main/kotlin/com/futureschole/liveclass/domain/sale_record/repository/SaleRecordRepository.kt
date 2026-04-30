package com.futureschole.liveclass.domain.sale_record.repository

import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import org.springframework.data.jpa.repository.JpaRepository

interface SaleRecordRepository: JpaRepository<SaleRecord, Long> {
}