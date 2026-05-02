package com.futureschole.liveclass.domain.sale_record.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class SaleRecordSearchDto @QueryProjection constructor(
    val id: Long,
    val course: CourseDto,
    val studentId: Long,
    val amount: Long,
    val paidAt: LocalDateTime,
    val createdAt: LocalDateTime
)

data class CourseDto @QueryProjection constructor(
    val id: Long,
    val creator: CreatorDto,
    val title: String
)

data class CreatorDto @QueryProjection constructor(
    val id: Long,
    val name: String
)
