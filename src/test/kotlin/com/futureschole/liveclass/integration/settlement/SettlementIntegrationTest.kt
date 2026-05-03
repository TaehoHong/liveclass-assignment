package com.futureschole.liveclass.integration.settlement

import com.futureschole.liveclass.domain.settlement.dto.SettlementMonthlyResponseDto
import com.futureschole.liveclass.integration.BaseIntegrationTest
import com.futureschole.liveclass.testdata.TestDataInserter
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.readValue
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
    }

    private fun readSettlementResponse(content: String): List<SettlementMonthlyResponseDto> =
        objectMapper.readValue<Array<SettlementMonthlyResponseDto>>(content).toList()
}
