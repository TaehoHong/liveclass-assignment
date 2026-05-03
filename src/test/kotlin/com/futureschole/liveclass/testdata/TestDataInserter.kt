package com.futureschole.liveclass.testdata

import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

@Component
class TestDataInserter(
    private val jdbcTemplate: JdbcTemplate
) {

    fun prepareSaleRecords() {
        insertSaleRecord(101L, 1L, 900001L, 30000L, LocalDateTime.of(2025, 2, 28, 23, 59))
        insertSaleRecord(102L, 1L, 900002L, 30000L, LocalDateTime.of(2025, 3, 1, 0, 0))
        insertSaleRecord(103L, 1L, 900003L, 40000L, LocalDateTime.of(2025, 3, 15, 12, 0))
        insertSaleRecord(104L, 1L, 900004L, 50000L, LocalDateTime.of(2025, 3, 31, 23, 59))
        insertSaleRecord(105L, 1L, 900005L, 50000L, LocalDateTime.of(2025, 4, 1, 0, 0))
        insertSaleRecord(106L, 3L, 900006L, 60000L, LocalDateTime.of(2025, 4, 1, 0, 0))
        insertSaleRecord(107L, 3L, 900007L, 60000L, LocalDateTime.of(2025, 3, 20, 12, 0))
    }

    fun prepareCancelRecords() {
        insertSaleRecord(301L, courseId = 1L, paidAt = LocalDateTime.of(2025, 2, 1, 10, 0))
        insertSaleRecord(302L, courseId = 3L, paidAt = LocalDateTime.of(2025, 2, 1, 10, 0))

        insertCancelRecord(400L, saleRecordId = 301L, cancelAt = LocalDateTime.of(2025, 2, 28, 23, 59))
        insertCancelRecord(401L, saleRecordId = 301L, cancelAt = LocalDateTime.of(2025, 3, 1, 0, 0))
        insertCancelRecord(402L, saleRecordId = 301L, cancelAt = LocalDateTime.of(2025, 3, 31, 23, 59))
        insertCancelRecord(403L, saleRecordId = 301L, cancelAt = LocalDateTime.of(2025, 4, 1, 0, 0))
        insertCancelRecord(404L, saleRecordId = 302L, cancelAt = LocalDateTime.of(2025, 3, 15, 12, 0))
    }

    fun prepareSettlements() {
        insertSettlement(
            id = 101L,
            creatorId = 1L,
            settlementMonth = LocalDate.of(2025, 3, 1),
            createdAt = LocalDateTime.of(2025, 4, 1, 10, 0)
        )
        insertSettlement(
            id = 102L,
            creatorId = 1L,
            settlementMonth = LocalDate.of(2025, 3, 1),
            createdAt = LocalDateTime.of(2025, 4, 2, 10, 0)
        )
        insertSettlement(
            id = 103L,
            creatorId = 1L,
            settlementMonth = LocalDate.of(2025, 4, 1),
            createdAt = LocalDateTime.of(2025, 5, 1, 9, 0)
        )
        insertSettlement(
            id = 104L,
            creatorId = 1L,
            settlementMonth = LocalDate.of(2025, 4, 1),
            createdAt = LocalDateTime.of(2025, 5, 2, 9, 0)
        )
        insertSettlement(
            id = 105L,
            creatorId = 1L,
            settlementMonth = LocalDate.of(2025, 4, 1),
            createdAt = LocalDateTime.of(2025, 5, 2, 9, 0)
        )
        insertSettlement(
            id = 106L,
            creatorId = 2L,
            settlementMonth = LocalDate.of(2025, 4, 1),
            createdAt = LocalDateTime.of(2025, 5, 3, 9, 0)
        )
    }

    fun prepareSaleRecordForIntegration() {
        insertSaleRecord(
            id = 701L,
            courseId = 1L,
            studentId = 900701L,
            amount = 30000L,
            paidAt = LocalDateTime.of(2025, 3, 1, 10, 0)
        )
    }

    fun prepareCancelableSaleRecord() {
        insertSaleRecord(
            id = 801L,
            courseId = 1L,
            studentId = 900801L,
            amount = 50000L,
            paidAt = LocalDateTime.of(2025, 3, 1, 10, 0)
        )
    }

    fun prepareCurrentMonthSettlementRecords() {
        val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))
        val paidAt = currentMonth.atDay(1).atTime(10, 0)

        insertSaleRecord(
            id = 501L,
            courseId = 1L,
            amount = 100000L,
            paidAt = paidAt
        )
        insertSaleRecord(
            id = 511L,
            courseId = 1L,
            amount = 50000L,
            paidAt = paidAt.plusHours(1)
        )
        insertSaleRecord(
            id = 512L,
            courseId = 3L,
            amount = 90000L,
            paidAt = paidAt.plusHours(2)
        )
        insertCancelRecord(
            id = 601L,
            saleRecordId = 501L,
            amount = 20000L,
            cancelAt = paidAt.plusHours(3)
        )
    }

    private fun insertSaleRecord(
        id: Long,
        courseId: Long,
        studentId: Long = 900000L + id,
        amount: Long = 50000L,
        paidAt: LocalDateTime
    ): Long {
        jdbcTemplate.update(
            """
                INSERT INTO sale_record (id, course_id, student_id, amount, paid_at)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            courseId,
            studentId,
            amount,
            Timestamp.valueOf(paidAt)
        )

        return id
    }

    private fun insertCancelRecord(
        id: Long,
        saleRecordId: Long,
        amount: Long = 10000L,
        cancelAt: LocalDateTime
    ): Long {
        jdbcTemplate.update(
            """
                INSERT INTO cancel_record (id, sale_record_id, amount, cancel_at)
                VALUES (?, ?, ?, ?)
            """.trimIndent(),
            id,
            saleRecordId,
            amount,
            Timestamp.valueOf(cancelAt)
        )

        return id
    }

    private fun insertSettlement(
        id: Long,
        creatorId: Long,
        settlementMonth: LocalDate,
        createdAt: LocalDateTime,
        status: SettlementStatus = SettlementStatus.PENDING,
        totalSaleAmount: Long = 100000L,
        totalCancelAmount: Long = 20000L,
        netSalesAmount: Long = 80000L,
        settlementAmount: Long = 64000L,
        saleCount: Long = 2L,
        cancelCount: Long = 1L,
        commissionRate: Short = 20,
        commissionAmount: Long = 16000L,
        carryoverDeductionAmount: Long = 0L,
        settledAt: LocalDateTime = createdAt,
        confirmedAt: LocalDateTime? = null,
        paidAt: LocalDateTime? = null
    ): Long {
        jdbcTemplate.update(
            """
                INSERT INTO settlement (
                    id,
                    creator_id,
                    status,
                    total_sale_amount,
                    total_cancel_amount,
                    net_sales_amount,
                    settlement_amount,
                    sale_count,
                    cancel_count,
                    commission_rate,
                    commission_amount,
                    carryover_deduction_amount,
                    settlement_month,
                    settled_at,
                    confirmed_at,
                    paid_at,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            creatorId,
            status.name,
            totalSaleAmount,
            totalCancelAmount,
            netSalesAmount,
            settlementAmount,
            saleCount,
            cancelCount,
            commissionRate,
            commissionAmount,
            carryoverDeductionAmount,
            Date.valueOf(settlementMonth),
            Timestamp.valueOf(settledAt),
            confirmedAt?.let { Timestamp.valueOf(it) },
            paidAt?.let { Timestamp.valueOf(it) },
            Timestamp.valueOf(createdAt)
        )

        return id
    }
}
