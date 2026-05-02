package com.futureschole.liveclass.integration.sale_record

import com.futureschole.liveclass.domain.course.repository.CourseRepository
import com.futureschole.liveclass.domain.sale_record.dto.CreationSaleRecordDto
import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordDto
import com.futureschole.liveclass.domain.sale_record.dto.SaleRecordSearchDto
import com.futureschole.liveclass.domain.sale_record.entity.SaleRecord
import com.futureschole.liveclass.domain.sale_record.repository.SaleRecordRepository
import com.futureschole.liveclass.integration.BaseIntegrationTest
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime

class SaleRecordIntegrationTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var saleRecordRepository: SaleRecordRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

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

            When("판매 내역 목록 조회 API를 호출하면") {
                Then("요청을 거부한다") {
                    mockMvc.perform(get("/api/sale-record"))
                        .andExpect(status().isForbidden)
                }
            }
        }

        Given("ADMIN 사용자가 있을 때") {
            When("판매 내역 목록 조회 API를 호출하면") {
                Then("배열 응답을 반환한다") {
                    saveSaleRecord()

                    val response = mockMvc.perform(
                        get("/api/sale-record")
                            .header("userId", "1")
                    )
                        .andExpect(status().isOk)
                        .andReturn()
                        .let { readSearchResponse(it.response.contentAsString) }

                    response.size shouldBe 1
                    response[0].course.creator.id shouldBe 1L
                }
            }
        }

        Given("CREATOR 사용자가 있을 때") {
            When("판매 내역 목록 조회 API를 호출하면") {
                Then("접근을 허용한다") {
                    saveSaleRecord()

                    val response = mockMvc.perform(
                        get("/api/sale-record")
                            .header("userId", "2")
                    )
                        .andExpect(status().isOk)
                        .andReturn()
                        .let { readSearchResponse(it.response.contentAsString) }

                    response.size shouldBe 1
                    response[0].course.creator.id shouldBe 1L
                }
            }
        }
    }

    private fun saveSaleRecord(
        courseId: Long = 1L,
        paidAt: LocalDateTime = LocalDateTime.of(2025, 3, 1, 10, 0)
    ): SaleRecord {
        val course = courseRepository.getReferenceById(courseId)

        return saleRecordRepository.save(
            SaleRecord(
                course = course,
                studentId = 900001L,
                amount = 30000L,
                paidAt = paidAt
            )
        )
    }

    private fun readSearchResponse(content: String): List<SaleRecordSearchDto> =
        objectMapper.readValue<Array<SaleRecordSearchDto>>(content).toList()
}
