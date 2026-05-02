package com.futureschole.liveclass.security

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.user.entity.User
import com.futureschole.liveclass.domain.user.entity.UserRole
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class AuthenticationHelperTest: BehaviorSpec({

    afterEach { SecurityContextHolder.clearContext() }

    Given("ADMIN 사용자가 인증되어 있을 때") {
        When("creatorId 없이 scope를 확인하면") {
            authenticate(User(id = 1L, role = UserRole.ADMIN))

            val result = withOwnedCreatorOrAdmin(null) { scopedCreatorId -> scopedCreatorId }

            Then("요청 creatorId를 그대로 action에 전달한다") {
                result shouldBe null
            }
        }

        When("creatorId와 함께 scope를 확인하면") {
            authenticate(User(id = 1L, role = UserRole.ADMIN))

            val result = withOwnedCreatorOrAdmin(1L) { scopedCreatorId -> scopedCreatorId }

            Then("요청 creatorId를 그대로 action에 전달한다") {
                result shouldBe 1L
            }
        }
    }

    Given("CREATOR 사용자가 인증되어 있을 때") {
        When("creatorId 없이 scope를 확인하면") {
            authenticate(User(id = 2L, role = UserRole.CREATOR, creatorId = 1L))

            val result = withOwnedCreatorOrAdmin(null) { scopedCreatorId -> scopedCreatorId }

            Then("본인의 creatorId를 action에 전달한다") {
                result shouldBe 1L
            }
        }

        When("본인 creatorId로 scope를 확인하면") {
            authenticate(User(id = 2L, role = UserRole.CREATOR, creatorId = 1L))

            val result = withOwnedCreatorOrAdmin(1L) { scopedCreatorId -> scopedCreatorId }

            Then("본인의 creatorId를 action에 전달한다") {
                result shouldBe 1L
            }
        }
    }

    Given("CREATOR 사용자가 다른 creatorId로 scope를 확인할 때") {
        authenticate(User(id = 2L, role = UserRole.CREATOR, creatorId = 1L))

        When("ownership helper를 호출하면") {
            var called = false
            val exception = shouldThrow<ApiException> {
                withOwnedCreatorOrAdmin(2L) {
                    called = true
                }
            }

            Then("FORBIDDEN을 반환하고 action을 실행하지 않는다") {
                exception.errorCode shouldBe ErrorCode.FORBIDDEN
                called shouldBe false
            }
        }
    }

    Given("인증 principal이 없을 때") {
        When("scope를 확인하면") {
            var called = false
            val exception = shouldThrow<ApiException> {
                withOwnedCreatorOrAdmin(null) {
                    called = true
                }
            }

            Then("UNAUTHORIZED를 반환하고 action을 실행하지 않는다") {
                exception.errorCode shouldBe ErrorCode.UNAUTHORIZED
                called shouldBe false
            }
        }
    }
})

private fun authenticate(user: User) {
    SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken(user, "", emptyList())
}
