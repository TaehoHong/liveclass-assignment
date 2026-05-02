package com.futureschole.liveclass.integration.cancel_record

import com.futureschole.liveclass.domain.cancel_record.dto.CancelRecordDto
import com.futureschole.liveclass.domain.cancel_record.dto.CreationCancelRecordDto
import com.futureschole.liveclass.domain.course.repository.CourseRepository
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import com.futureschole.liveclass.integration.BaseIntegrationTest
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class CancelRecordIntegrationTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var saleRecordRepository: SaleRecordRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

    init {
        Given("존재하는 판매 내역과 인증된 사용자가 있을 때") {
            When("취소 내역 등록 API를 호출하면") {
                Then("취소 내역을 DB에 저장한다") {
                    val saleRecord = saveSaleRecord()
                    val request = CreationCancelRecordDto(
                        saleRecordId = saleRecord.id,
                        amount = 20000L,
                        cancelAt = saleRecord.paidAt.plusDays(1)
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
                    response.saleRecordId shouldBe saleRecord.id
                    response.amount shouldBe request.amount
                    response.cancelAt shouldBe request.cancelAt
                }
            }
        }

        Given("[CR-INT-002] 인증되지 않은 사용자가 있을 때") {
            When("취소 내역 등록 API를 호출하면") {
                Then("요청을 거부하고 취소 내역을 저장하지 않는다") {
                    val saleRecord = saveSaleRecord()
                    val request = CreationCancelRecordDto(
                        saleRecordId = saleRecord.id,
                        amount = 20000L,
                        cancelAt = saleRecord.paidAt.plusDays(1)
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

    private fun saveSaleRecord(): SaleRecord {
        return saleRecordRepository.saveAndFlush(
            SaleRecord(
                course = courseRepository.getReferenceById(1L),
                studentId = 1L,
                amount = 50000L,
                paidAt = LocalDateTime.now().minusDays(2).withNano(0)
            )
        )
    }
}
