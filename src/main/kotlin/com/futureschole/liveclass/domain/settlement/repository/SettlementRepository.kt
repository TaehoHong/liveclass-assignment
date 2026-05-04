package com.futureschole.liveclass.domain.settlement.repository

import com.futureschole.liveclass.domain.settlement.dto.QSettlementAmountDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementAmountDto
import com.futureschole.liveclass.domain.settlement.entity.QSettlement.Companion.settlement
import com.futureschole.liveclass.domain.settlement.entity.Settlement
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.YearMonth

interface SettlementRepository: JpaRepository<Settlement, Long>, QSettlementRepository {
    fun existsByCreatorIdAndSettlementMonth(creatorId: Long, settlementMonth: LocalDate): Boolean
}

interface QSettlementRepository {
    fun findAll(creatorId: Long?, settlementMonth: YearMonth?): List<Settlement>
    fun findAmountByCreatorIdAndSettlementMonth(creatorId: Long, settlementMonth: YearMonth): Long?
    fun findCreatorIdToSettlement(
        startSettlementMonth: YearMonth,
        endSettlementMonth: YearMonth
    ): Map<Long, List<SettlementAmountDto>>
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

    override fun findAmountByCreatorIdAndSettlementMonth(creatorId: Long, settlementMonth: YearMonth): Long? {
        return queryFactory
            .select(settlement.settlementAmount)
            .from(settlement)
            .where(
                creatorIdEq(creatorId),
                settlementMonthEq(settlementMonth)
            )
            .fetchOne()
    }

    private fun creatorIdEq(creatorId: Long?) =
        creatorId?.let { settlement.creatorId.eq(it) }

    private fun settlementMonthEq(settlementMonth: YearMonth?) =
        settlementMonth?.let { settlement.settlementMonth.eq(it.atDay(1)) }

    override fun findCreatorIdToSettlement(
        startSettlementMonth: YearMonth,
        endSettlementMonth: YearMonth
    ): Map<Long, List<SettlementAmountDto>> {
        return queryFactory
            .select(
                QSettlementAmountDto(
                    settlement.creatorId,
                    settlement.settlementMonth,
                    settlement.settlementAmount
                )
            )
            .from(settlement)
            .where(
                settlement.settlementMonth.goe(startSettlementMonth.atDay(1)),
                settlement.settlementMonth.loe(endSettlementMonth.atDay(1)),
            )
            .orderBy(
                settlement.creatorId.asc(),
                settlement.settlementMonth.asc()
            )
            .fetch()
            .groupBy { it.creatorId }
    }
}
