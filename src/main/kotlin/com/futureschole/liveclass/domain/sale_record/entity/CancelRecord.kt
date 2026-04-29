package com.futureschole.liveclass.domain.sale_record.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Table(name = "cancel_record")
@Entity
class CancelRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "sale_record_id")
    val saleRecordId: Long,

    @Column(name = "amount")
    val amount: Int,

    @Column(name = "cancel_at")
    val cancelAt: LocalDateTime,

    @Column(name = "created_at")
    val createdAt: LocalDateTime
)
