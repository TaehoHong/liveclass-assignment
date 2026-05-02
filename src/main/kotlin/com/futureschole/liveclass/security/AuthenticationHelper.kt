package com.futureschole.liveclass.security

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.user.entity.User
import com.futureschole.liveclass.domain.user.entity.UserRole
import org.springframework.security.core.context.SecurityContextHolder

fun <T> withOwnedCreatorOrAdmin(creatorId: Long?, action: (Long?) -> T): T {
    val user = getCurrentUser()

    return when (user.role) {
        UserRole.ADMIN -> action(creatorId)
        UserRole.CREATOR -> {
            val ownedCreatorId = user.creatorId ?: throw ApiException(ErrorCode.FORBIDDEN)

            if (creatorId != null && creatorId != ownedCreatorId) {
                throw ApiException(ErrorCode.FORBIDDEN)
            }

            action(ownedCreatorId)
        }
    }
}

private fun getCurrentUser(): User {
    return SecurityContextHolder.getContext()
        .authentication
        ?.principal as? User
        ?: throw ApiException(ErrorCode.UNAUTHORIZED)
}
