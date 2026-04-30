package com.futureschole.liveclass.integration.sale_record

import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordDto
import com.futureschole.liveclass.integration.BaseIntegrationTest
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class SaleRecordIntegrationTest: BaseIntegrationTest() {

    init {
        Given("존재하는 강의와 인증된 사용자가 있을 때") {
            When("판매 내역 등록 API를 호출하면") {
                Then("응답 DTO를 반환하고 판매 내역을 DB에 저장한다") {
                    val courseId = 1L
                    val studentId = 900_001L
                    val amount = 30_000L
                    val paidAt = LocalDateTime.of(2026, 4, 30, 10, 15, 0)
                    val request = mapOf(
                        "courseId" to courseId,
                        "studentId" to studentId,
                        "amount" to amount,
                        "paidAt" to paidAt.toString()
                    )

                    val response = mockMvc.perform(
                        post("/api/sale-record")
                            .header("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isOk)
                        .andReturn().let {
                            objectMapper.readValue(it.response.contentAsString, SaleRecordDto::class.java)
                        }


                    response.id shouldBeGreaterThan 0
                    response.courseId shouldBe courseId
                    response.studentId shouldBe studentId
                    response.amount shouldBe amount
                    response.paidAt shouldBe paidAt
                }
            }
        }

        Given("인증되지 않은 사용자가 있을 때") {
            When("판매 내역 등록 API를 호출하면") {
                Then("요청을 거부하고 판매 내역을 저장하지 않는다") {
                    val studentId = 900_002L
                    val request = mapOf(
                        "courseId" to 1L,
                        "studentId" to studentId,
                        "amount" to 30_000L,
                        "paidAt" to LocalDateTime.of(2026, 4, 30, 11, 0, 0).toString()
                    )

                    mockMvc.perform(
                        post("/api/sale-record")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isForbidden)
                }
            }
        }
    }
}
