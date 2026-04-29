package com.futureschole.liveclass.domain.settlement.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table(name = "settlement")
@Entity
class Settlement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "creator_id")
    val creatorId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: SettlementStatus,

    @Column(name = "total_sale_amount")
    val totalSaleAmount: Int,

    @Column(name = "total_cancel_amount")
    val totalCancelAmount: Int,

    @Column(name = "net_sales_amount")
    val netSalesAmount: Int,

    @Column(name = "settlement_amount")
    val settlementAmount: Int,

    @Column(name = "sale_count")
    val saleCount: Int,

    @Column(name = "cancel_count")
    val cancelCount: Int,

    @Column(name = "commission_rate")
    val commissionRate: Short,

    @Column(name = "commission_amount")
    val commissionAmount: Int,

    @Column(name = "carryover_deduction_amount")
    val carryoverDeductionAmount: Int,

    @Column(name = "settlement_month")
    val settlementMonth: LocalDate,

    @Column(name = "settled_at")
    val settledAt: LocalDateTime,

    @Column(name = "confirmed_at")
    val confirmedAt: LocalDateTime?,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime
)

enum class SettlementStatus {
    PENDING,
    CONFIRMED,
    PAID
}
