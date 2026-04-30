package com.futureschole.liveclass.common.config

import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.OffsetDateTime

@Component
class SampleDataInitializer(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val creatorCount = jdbcTemplate.queryForObject<Long>("SELECT COUNT(*) FROM creator") ?: 0L

        if (creatorCount > 0) {
            return
        }

        ClassPathResource("sample-data.json")
            .inputStream
            .use { objectMapper.readValue<SampleData>(it) }
            .apply {
                saveCreators(this.creators)
                saveCourses(this.courses)
                saveSaleRecord(this.saleRecords)
                saveCancelRecord(this.cancelRecords)
            }
    }

    private fun saveCreators(creators: List<SampleCreator>) {
        batchUpdate("INSERT INTO creator (id, name) VALUES (?, ?)", creators) { ps, creator ->
            ps.setLong(1, creator.id.toNumberId())
            ps.setString(2, creator.name)
        }
    }

    private fun saveCourses(courses: List<SampleCourse>) {
        batchUpdate("INSERT INTO course (id, creator_id, title) VALUES (?, ?, ?)", courses) { ps, course ->
            ps.setLong(1, course.id.toNumberId())
            ps.setLong(2, course.creatorId.toNumberId())
            ps.setString(3, course.title)
        }
    }

    private fun saveSaleRecord(saleRecords: List<SampleSaleRecord>) {
        batchUpdate(
            """
                INSERT INTO sale_record (id, course_id, student_id, amount, paid_at)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            saleRecords
        ) { ps, saleRecord ->
            ps.setLong(1, saleRecord.id.toNumberId())
            ps.setLong(2, saleRecord.courseId.toNumberId())
            ps.setLong(3, saleRecord.studentId.toNumberId())
            ps.setInt(4, saleRecord.amount)
            ps.setTimestamp(5, saleRecord.paidAt.toTimestamp())
        }
    }

    private fun saveCancelRecord(cancelRecords: List<SampleCancelRecord>) {
        batchUpdate(
            """
                INSERT INTO cancel_record (id, sale_record_id, amount, cancel_at)
                VALUES (?, ?, ?, ?)
            """.trimIndent(),
            cancelRecords
        ) { ps, cancelRecord ->
            ps.setLong(1, cancelRecord.id.toNumberId())
            ps.setLong(2, cancelRecord.saleRecordId.toNumberId())
            ps.setInt(3, cancelRecord.amount)
            ps.setTimestamp(4, cancelRecord.cancelAt.toTimestamp())
        }
    }

    private fun <T> batchUpdate(
        sql: String,
        values: List<T>,
        bind: (PreparedStatement, T) -> Unit
    ) {
        if (values.isEmpty()) return

        jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    bind(ps, values[i])
                }

                override fun getBatchSize(): Int = values.size
            }
        )
    }
}


private fun String.toNumberId(): Long = substringAfterLast("-").toLong()

private fun String.toTimestamp(): Timestamp =
    Timestamp.valueOf(OffsetDateTime.parse(this).toLocalDateTime())

private data class SampleData(
    val creators: List<SampleCreator>,
    val courses: List<SampleCourse>,
    val saleRecords: List<SampleSaleRecord>,
    val cancelRecords: List<SampleCancelRecord> = emptyList()
)

private data class SampleCreator(
    val id: String,
    val name: String
)

private data class SampleCourse(
    val id: String,
    val creatorId: String,
    val title: String
)

private data class SampleSaleRecord(
    val _comment: String? = null,
    val id: String,
    val courseId: String,
    val studentId: String,
    val amount: Int,
    val paidAt: String
)

private data class SampleCancelRecord(
    val id: String,
    val saleRecordId: String,
    val amount: Int,
    val cancelAt: String
)
