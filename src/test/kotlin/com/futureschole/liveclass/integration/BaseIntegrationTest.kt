package com.futureschole.liveclass.integration

import com.futureschole.liveclass.config.DatabaseTruncator
import com.futureschole.liveclass.config.TestcontainersConfiguration
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import tools.jackson.databind.ObjectMapper
import javax.sql.DataSource

@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
open class BaseIntegrationTest: BehaviorSpec() {

    override val extensions: List<Extension> = listOf(SpringExtension())

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    protected lateinit var databaseTruncator: DatabaseTruncator

    init {
        afterEach { databaseTruncator.truncate() }
    }
}