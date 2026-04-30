package com.futureschole.liveclass.config

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource

@Component
class DatabaseTruncator(
    private val dataSource: DataSource,
    private val entityManager: EntityManager
) {

    private val protectedTables = listOf("creator", "course")


    @Transactional
    fun truncate() {
        try {
            setForeignKeyCheck(0)

            this.tableNames
                .filter(::canTruncateTable)
                .forEach { tableName ->
                    entityManager.createNativeQuery("TRUNCATE TABLE `$tableName`").executeUpdate()
                }
        } finally {
            setForeignKeyCheck(1)
        }
    }

    private fun setForeignKeyCheck(mode: Int) {
        entityManager.createNativeQuery(String.format("SET FOREIGN_KEY_CHECKS = %d", mode)).executeUpdate()
    }

    val tableNames: List<String> =
        dataSource.connection.use { connection ->
            connection.metaData.getTables(
                null, connection.schema, "%", arrayOf<String>("TABLE")
            ).use { rs ->
                buildList {
                    while (rs.next()) {
                        add(rs.getString("TABLE_NAME"))
                    }
                }

            }
        }

    private fun canTruncateTable(tableName: String) = !protectedTables.contains(tableName)
}
