package com.futureschole.liveclass

import com.futureschole.liveclass.config.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class LiveclassApplicationTests {

    @Test
    fun contextLoads() {
    }

}
