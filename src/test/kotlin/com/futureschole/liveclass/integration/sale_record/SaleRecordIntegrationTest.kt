package com.futureschole.liveclass.integration.sale_record

import com.futureschole.liveclass.domain.sale_record.dto.CreationSaleRecordDto
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
                    val request = CreationSaleRecordDto(
                        courseId = 1L,
                        studentId = 1L,
                        amount =  30000L,
                        paidAt =  LocalDateTime.of(2026, 4, 30, 10, 15, 0)
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
                    response.courseId shouldBe request.courseId
                    response.studentId shouldBe request.studentId
                    response.amount shouldBe request.amount
                    response.paidAt shouldBe request.paidAt
                }
            }
        }

        Given("인증되지 않은 사용자가 있을 때") {
            When("판매 내역 등록 API를 호출하면") {
                Then("요청을 거부하고 판매 내역을 저장하지 않는다") {
                    val request = CreationSaleRecordDto(
                        courseId = 1L,
                        studentId = 1L,
                        amount =  30000L,
                        paidAt =  LocalDateTime.of(2026, 4, 30, 10, 15, 0)
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
