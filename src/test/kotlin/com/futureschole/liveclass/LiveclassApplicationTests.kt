package com.futureschole.liveclass

import com.futureschole.liveclass.integration.BaseIntegrationTest

class LiveclassApplicationTests: BaseIntegrationTest() {
    init {
        Given("애플리케이션 컨텍스트가 필요할 때") {
            When("테스트가 시작되면") {
                Then("공유 Spring 컨텍스트를 로드한다") {
                }
            }
        }
    }
}
