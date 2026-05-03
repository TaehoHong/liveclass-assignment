package com.futureschole.liveclass.domain.cancel_record.repository

import com.futureschole.liveclass.common.entity.QCreator.Companion.creator
import com.futureschole.liveclass.domain.cancel_record.entity.CancelRecord
import com.futureschole.liveclass.domain.cancel_record.entity.QCancelRecord.Companion.cancelRecord
import com.futureschole.liveclass.domain.course.entity.QCourse.Companion.course
import com.futureschole.liveclass.domain.sale_record.entity.QSaleRecord.Companion.saleRecord
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.YearMonth

interface CancelRecordRepository: JpaRepository<CancelRecord, Long>, QCancelRecordRepository {
    fun findAllBySaleRecordId(saleRecordId: Long): List<CancelRecord>
}

interface QCancelRecordRepository {
    fun findCreatorIdToCancelRecords(
        creatorId: Long?,
        month: YearMonth
    ): Map<Long, List<CancelRecord>>
}

@Repository
class QCancelRecordRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): QCancelRecordRepository {

    override fun findCreatorIdToCancelRecords(
        creatorId: Long?,
        month: YearMonth
    ): Map<Long, List<CancelRecord>> {
        val start = month.atDay(1).atStartOfDay()
        val endExclusive = month.plusMonths(1).atDay(1).atStartOfDay()

        return queryFactory
            .selectFrom(cancelRecord)
            .innerJoin(cancelRecord.saleRecord, saleRecord).fetchJoin()
            .innerJoin(saleRecord.course, course).fetchJoin()
            .innerJoin(course.creator, creator).fetchJoin()
            .where(
                creatorIdEq(creatorId),
                cancelRecord.cancelAt.goe(start),
                cancelRecord.cancelAt.lt(endExclusive),
            )
            .orderBy(creator.id.asc(), cancelRecord.id.asc())
            .fetch()
            .groupBy { requireNotNull(it.saleRecord).course.creator.id }
    }

    private fun creatorIdEq(creatorId: Long?) =
        creatorId?.let { creator.id.eq(it) }
}
