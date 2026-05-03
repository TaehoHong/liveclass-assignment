package com.futureschole.liveclass.domain.settlement.repository

import com.futureschole.liveclass.domain.settlement.entity.QSettlement.Companion.settlement
import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.YearMonth

interface SettlementRepository: JpaRepository<Settlement, Long>, QSettlementRepository {
}

interface QSettlementRepository {
    fun findAll(creatorId: Long?, settlementMonth: YearMonth?): List<Settlement>
}

@Repository
class QSettlementRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): QSettlementRepository {

    override fun findAll(creatorId: Long?, settlementMonth: YearMonth?): List<Settlement> {
        return queryFactory
            .selectFrom(settlement)
            .where(
                creatorIdEq(creatorId),
                settlementMonthEq(settlementMonth)
            )
            .orderBy(
                settlement.settlementMonth.desc(),
                settlement.creatorId.asc(),
                settlement.createdAt.desc(),
                settlement.id.desc()
            )
            .fetch()
    }

    private fun creatorIdEq(creatorId: Long?) =
        creatorId?.let { settlement.creatorId.eq(it) }

    private fun settlementMonthEq(settlementMonth: YearMonth?) =
        settlementMonth?.let { settlement.settlementMonth.eq(it.atDay(1)) }
}
