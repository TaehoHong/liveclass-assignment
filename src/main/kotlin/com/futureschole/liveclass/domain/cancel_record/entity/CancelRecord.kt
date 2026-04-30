package com.futureschole.liveclass.domain.cancel_record.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "cancel_record")
@Entity
class CancelRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "sale_record_id")
    val saleRecordId: Long,

    @Column(name = "amount")
    val amount: Long,

    @Column(name = "cancel_at")
    val cancelAt: LocalDateTime,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
