package com.futureschole.liveclass.domain.sale_record.repository

import com.futureschole.liveclass.common.entity.QCreator.Companion.creator
import com.futureschole.liveclass.domain.course.entity.QCourse.Companion.course
import com.futureschole.liveclass.domain.sale_record.dto.QCourseDto
import com.futureschole.liveclass.domain.sale_record.dto.QCreatorDto
import com.futureschole.liveclass.domain.sale_record.dto.QSaleRecordSearchDto
import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordSearchDto
import com.futureschole.liveclass.domain.sale_record.entity.QSaleRecord.Companion.saleRecord
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.YearMonth

interface SaleRecordRepository: JpaRepository<SaleRecord, Long>, QSaleRecordRepository {
}

interface QSaleRecordRepository {
    fun findAllByCreatorAndPaidAtRange(
        creatorId: Long?,
        startPaidAt: LocalDateTime?,
        endPaidAt: LocalDateTime?
    ): List<SaleRecordSearchDto>

    fun findCreatorIdToSaleRecords(
        creatorId: Long?,
        month: YearMonth
    ): Map<Long, List<SaleRecord>>
}

@Repository
class QSaleRecordRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
): QSaleRecordRepository {

    override fun findAllByCreatorAndPaidAtRange(
        creatorId: Long?,
        startPaidAt: LocalDateTime?,
        endPaidAt: LocalDateTime?,
    ): List<SaleRecordSearchDto> {
        return queryFactory
            .select(
                QSaleRecordSearchDto(
                    saleRecord.id,
                    QCourseDto(
                        course.id,
                        QCreatorDto(creator.id, creator.name),
                        course.title
                    ),
                    saleRecord.studentId,
                    saleRecord.amount,
                    saleRecord.paidAt,
                    saleRecord.createdAt
                )
            )
            .from(saleRecord)
            .innerJoin(saleRecord.course, course)
            .innerJoin(course.creator, creator)
            .where(
                creatorIdEq(creatorId),
                paidAtGoe(startPaidAt),
                paidAtLt(endPaidAt),
            )
            .orderBy(saleRecord.paidAt.desc(), saleRecord.id.desc())
            .fetch()
    }

    override fun findCreatorIdToSaleRecords(
        creatorId: Long?,
        month: YearMonth
    ): Map<Long, List<SaleRecord>> {
        val start = month.atDay(1).atStartOfDay()
        val endExclusive = month.plusMonths(1).atDay(1).atStartOfDay()

        return queryFactory
            .selectFrom(saleRecord)
            .innerJoin(saleRecord.course, course).fetchJoin()
            .innerJoin(course.creator, creator).fetchJoin()
            .where(
                creatorIdEq(creatorId),
                saleRecord.paidAt.goe(start),
                saleRecord.paidAt.lt(endExclusive),
            )
            .orderBy(creator.id.asc(), saleRecord.id.asc())
            .fetch()
            .groupBy { it.course.creator.id }
    }

    private fun creatorIdEq(creatorId: Long?) =
        creatorId?.let { creator.id.eq(it) }

    private fun paidAtGoe(startAt: LocalDateTime?) =
        startAt?.let { saleRecord.paidAt.goe(it) }

    private fun paidAtLt(endAtExclusive: LocalDateTime?) =
        endAtExclusive?.let { saleRecord.paidAt.lt(it) }
}
