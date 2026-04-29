package com.futureschole.liveclass.domain.sale_record.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Table(name = "sale_record")
@Entity
class SaleRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "course_id")
    val courseId: Long,

    @Column(name = "student_id")
    val studentId: Long,

    @Column(name = "amount")
    val amount: Int,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime,

    @Column(name = "created_at")
    val createdAt: LocalDateTime
)
