package com.futureschole.liveclass.integration.settlement

import com.futureschole.liveclass.domain.settlement.dto.SettlementAmountDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementSummaryItemDto
import com.futureschole.liveclass.domain.settlement.dto.SettlementSummaryResponseDto
import com.futureschole.liveclass.domain.settlement.entity.SettlementStatus
import com.futureschole.liveclass.integration.BaseIntegrationTest
import com.futureschole.liveclass.testdata.TestDataInserter
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class SettlementIntegrationTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var testDataInserter: TestDataInserter

    init {
        Given("현재월 creator1의 판매와 취소 데이터가 있을 때") {
            When("ADMIN이 creator1 정산을 조회하면") {
                Then("creator1의 현재월 예상 정산 row를 반환한다") {
                    testDataInserter.prepareCurrentMonthSettlementRecords()

                    val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

                    val response = mockMvc.perform(
                        get("/api/settlement")
                            .header("userId", "1")
                            .param("month", currentMonth.toString())
                            .param("creatorId", "1")
                    )
                        .andExpect(status().isOk)
                        .andReturn()
                        .let { readSettlementResponse(it.response.contentAsString) }

                    response.size shouldBe 1

                    val settlement = response[0]
                    settlement.creatorId shouldBe 1L
                    settlement.settlementMonth shouldBe currentMonth.atDay(1)
                    settlement.settlementId shouldBe null
                    settlement.status shouldBe null
                    settlement.totalSaleAmount shouldBe 150000L
                    settlement.totalCancelAmount shouldBe 20000L
                    settlement.netSalesAmount shouldBe 130000L
                    settlement.commissionRate shouldBe 20.toShort()
                    settlement.commissionAmount shouldBe 26000L
                    settlement.settlementAmount shouldBe 104000L
                    settlement.saleCount shouldBe 2L
                    settlement.cancelCount shouldBe 1L
                }
            }
        }

        Given("현재월 creator1과 creator2의 판매 데이터가 있을 때") {
            When("CREATOR가 creatorId 없이 정산을 조회하면") {
                Then("인증된 사용자의 예상 정산 row만 반환한다") {
                    testDataInserter.prepareCurrentMonthSettlementRecords()

                    val currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"))

                    val response = mockMvc.perform(
                        get("/api/settlement")
                            .header("userId", "2")
                            .param("month", currentMonth.toString())
                    )
                        .andExpect(status().isOk)
                        .andReturn()
                        .let { readSettlementResponse(it.response.contentAsString) }

                    response.size shouldBe 1

                    val settlement = response[0]
                    settlement.creatorId shouldBe 1L
                    settlement.settlementMonth shouldBe currentMonth.atDay(1)
                    settlement.settlementId shouldBe null
                    settlement.status shouldBe null
                    settlement.totalSaleAmount shouldBe 150000L
                    settlement.totalCancelAmount shouldBe 20000L
                    settlement.netSalesAmount shouldBe 130000L
                    settlement.commissionAmount shouldBe 26000L
                    settlement.settlementAmount shouldBe 104000L
                    settlement.saleCount shouldBe 2L
                    settlement.cancelCount shouldBe 1L
                }
            }
        }

        Given("ADMIN 사용자와 월 판매 데이터가 있을 때") {
            When("정산 생성 API를 호출하면") {
                Then("PENDING 정산을 생성하고 응답한다") {
                    val month = YearMonth.of(2025, 3)

                    testDataInserter.prepareSaleRecords()

                    val response = mockMvc.perform(
                        post("/api/settlement")
                            .header("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""{"creatorId":1,"month":"$month"}""")
                    )
                        .andExpect(status().isOk)
                        .andReturn()
                        .let { objectMapper.readValue(it.response.contentAsString, SettlementMonthlyResponseDto::class.java) }

                    response.creatorId shouldBe 1L
                    response.settlementMonth shouldBe month.atDay(1)
                    response.status shouldBe SettlementStatus.PENDING
                    (response.settlementId != null) shouldBe true
                }
            }
        }

        Given("CREATOR 사용자가 있을 때") {
            When("정산 생성 API를 호출하면") {
                Then("접근을 거부한다") {
                    mockMvc.perform(
                        post("/api/settlement")
                            .header("userId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""{"creatorId":1,"month":"2025-03"}""")
                    )
                        .andExpect(status().isForbidden)
                }
            }
        }

        Given("과거월 정산 데이터가 있을 때") {
            When("ADMIN이 summary를 조회하면") {
                Then("creator별 월별 정산 예정 금액 목록과 합계를 반환한다") {
                    testDataInserter.prepareSettlements()

                    val response = mockMvc.perform(
                        get("/api/settlement/summary")
                            .header("userId", "1")
                            .param("startMonth", "2025-03")
                            .param("endMonth", "2025-04")
                    )
                        .andExpect(status().isOk)
                        .andReturn()
                        .let { readSummaryResponse(it.response.contentAsString) }

                    response shouldBe getExpectedResponse()
                }
            }
        }

        Given("CREATOR 사용자가 summary를 조회할 때") {
            When("summary API를 호출하면") {
                Then("접근을 거부한다") {
                    mockMvc.perform(
                        get("/api/settlement/summary")
                            .header("userId", "2")
                            .param("startMonth", "2025-03")
                            .param("endMonth", "2025-04")
                    )
                        .andExpect(status().isForbidden)
                }
            }
        }
    }

    private fun getExpectedResponse(): SettlementSummaryResponseDto = SettlementSummaryResponseDto(
        settlements = listOf(
            SettlementSummaryItemDto(
                creatorId = 1L,
                settlements = listOf(
                    SettlementAmountDto(1L, LocalDate.of(2025, 4, 1), 64000L),
                    SettlementAmountDto(1L, LocalDate.of(2025, 3, 1), 64000L)
                ),
                totalSettlementAmount = 128000L
            ),
            SettlementSummaryItemDto(
                creatorId = 2L,
                settlements = listOf(
                    SettlementAmountDto(2L, LocalDate.of(2025, 4, 1), 64000L),
                    SettlementAmountDto(2L, LocalDate.of(2025, 3, 1), 64000L)
                ),
                totalSettlementAmount = 128000L
            ),
            SettlementSummaryItemDto(
                creatorId = 3L,
                settlements = listOf(
                    SettlementAmountDto(3L, LocalDate.of(2025, 4, 1), 64000L)
                ),
                totalSettlementAmount = 64000L
            )
        ),
        totalSettlementAmount = 320000L
    )

    private fun readSettlementResponse(content: String): List<SettlementMonthlyResponseDto> =
        objectMapper.readValue<Array<SettlementMonthlyResponseDto>>(content).toList()

    private fun readSummaryResponse(content: String): SettlementSummaryResponseDto =
        objectMapper.readValue(content)
}
