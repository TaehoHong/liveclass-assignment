package com.futureschole.liveclass.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mysqlContainer() = MySQLContainer(DockerImageName.parse("mysql:latest"))
        .apply {
            withDatabaseName("testdb")
            withUsername("liveclass_test")
            withPassword("liveclass_test")
            withInitScript("db/db_schema.sql")
        }

}