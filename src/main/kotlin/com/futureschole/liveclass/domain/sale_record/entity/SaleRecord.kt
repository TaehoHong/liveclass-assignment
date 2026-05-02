package com.futureschole.liveclass.domain.sale_record.entity

import com.futureschole.liveclass.domain.course.entity.Course
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Table(name = "sale_record")
@Entity
class SaleRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    val course: Course,

    @Column(name = "student_id")
    val studentId: Long,

    @Column(name = "amount")
    val amount: Long,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
