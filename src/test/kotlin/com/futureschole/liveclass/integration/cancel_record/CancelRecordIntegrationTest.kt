package com.futureschole.liveclass.integration.cancel_record

import com.futureschole.liveclass.domain.cancel_record.dto.CancelRecordDto
import com.futureschole.liveclass.domain.cancel_record.dto.CreationCancelRecordDto
import com.futureschole.liveclass.integration.BaseIntegrationTest
import com.futureschole.liveclass.testdata.TestDataInserter
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class CancelRecordIntegrationTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var testDataInserter: TestDataInserter

    init {
        Given("존재하는 판매 내역과 인증된 사용자가 있을 때") {
            When("취소 내역 등록 API를 호출하면") {
                Then("취소 내역을 DB에 저장한다") {
                    testDataInserter.prepareCancelableSaleRecord()
                    val request = CreationCancelRecordDto(
                        saleRecordId = 801L,
                        amount = 20000L,
                        cancelAt = LocalDateTime.of(2025, 3, 2, 10, 0)
                    )

                    val response = mockMvc.perform(
                        post("/api/cancel-record")
                            .header("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    ).andExpect(status().isOk)
                        .andReturn().let {
                            objectMapper.readValue(it.response.contentAsString, CancelRecordDto::class.java)
                    }

                    response.id shouldBeGreaterThan 0
                    response.saleRecordId shouldBe 801L
                    response.amount shouldBe request.amount
                    response.cancelAt shouldBe request.cancelAt
                }
            }
        }

        Given("[CR-INT-002] 인증되지 않은 사용자가 있을 때") {
            When("취소 내역 등록 API를 호출하면") {
                Then("요청을 거부하고 취소 내역을 저장하지 않는다") {
                    testDataInserter.prepareCancelableSaleRecord()
                    val request = CreationCancelRecordDto(
                        saleRecordId = 801L,
                        amount = 20000L,
                        cancelAt = LocalDateTime.of(2025, 3, 2, 10, 0)
                    )

                    mockMvc.perform(
                        post("/api/cancel-record")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isForbidden)
                }
            }
        }
    }
}
