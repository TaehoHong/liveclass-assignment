package com.futureschole.liveclass.unit.controller.sale_record

import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.common.exception.ExceptionResponse
import com.futureschole.liveclass.domain.sale_record.controller.SaleRecordController
import com.futureschole.liveclass.domain.sale_record.service.SaleRecordService
import com.futureschole.liveclass.security.filter.AuthenticationFilter
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [SaleRecordController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [AuthenticationFilter::class])
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class SaleRecordControllerValidationTest: BehaviorSpec() {

    override val extensions: List<Extension> = listOf(SpringExtension())

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var saleRecordService: SaleRecordService

    init {
        Given("잘못된 숫자 필드가 포함된 판매 내역 생성 요청이 있을 때") {
            val request = mapOf(
                "courseId" to 0L,
                "studentId" to 0L,
                "amount" to -1L,
                "paidAt" to LocalDateTime.of(2026, 4, 30, 10, 15, 0).toString()
            )

            When("판매 내역 등록 API를 호출하면") {
                val response = mockMvc.perform(
                    post("/api/sale-record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andReturn()
                    .let {
                        objectMapper.readValue(it.response.contentAsString, ExceptionResponse::class.java)
                    }

                Then("필드 검증 실패 응답을 반환하고 서비스는 호출하지 않는다") {
                    response.errorCode shouldBe ErrorCode.BAD_REQUEST_FIELD_VALID_ERROR.code
                    response.message shouldBe ErrorCode.BAD_REQUEST_FIELD_VALID_ERROR.message

                    val extra = response.extra ?: error("Validation error extra must not be null")
                    extra.keys shouldBe setOf("courseId", "studentId", "amount")

                    verifyNoInteractions(saleRecordService)
                }
            }
        }
    }
}
