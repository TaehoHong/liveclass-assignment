package com.futureschole.liveclass.unit.controller.sale_record

import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.common.exception.ExceptionResponse
import com.futureschole.liveclass.integration.BaseIntegrationTest
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class SaleRecordControllerTest: BaseIntegrationTest() {
    init {
        Given("잘못된 숫자 필드가 포함된 판매 내역 생성 요청이 있을 때") {
            val request = mapOf(
                "courseId" to 0L,
                "studentId" to 0L,
                "amount" to -1L,
                "paidAt" to LocalDateTime.of(2026, 4, 30, 10, 15, 0).toString()
            )

            When("판매 내역 등록 API를 호출하면") {
                Then("필드 검증 실패 응답을 반환한다") {
                    val response = mockMvc.perform(
                        post("/api/sale-record")
                            .header("userId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isBadRequest)
                        .andReturn()
                        .let {
                            objectMapper.readValue(it.response.contentAsString, ExceptionResponse::class.java)
                        }

                    response.errorCode shouldBe ErrorCode.BAD_REQUEST_FIELD_VALID_ERROR.code
                    response.message shouldBe ErrorCode.BAD_REQUEST_FIELD_VALID_ERROR.message

                    val extra = response.extra ?: error("Validation error extra must not be null")
                    extra.keys shouldBe setOf("courseId", "studentId", "amount")
                }
            }
        }
    }
}
