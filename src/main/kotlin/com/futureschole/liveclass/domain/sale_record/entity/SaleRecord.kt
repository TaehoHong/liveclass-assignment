package com.futureschole.liveclass.domain.sale_record.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "sale_record")
@Entity
class SaleRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "course_id")
    val courseId: Long,

    @Column(name = "student_id")
    val studentId: Long,

    @Column(name = "amount")
    val amount: Long,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
