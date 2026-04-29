package com.futureschole.liveclass.domain.user.service

import com.futureschole.liveclass.domain.user.entity.User
import com.futureschole.liveclass.domain.user.entity.UserRole
import org.springframework.stereotype.Service

@Service
class UserService {

    val users = listOf(
        User(1, UserRole.ADMIN), User(2, UserRole.USER)
    )

    
    fun getUserById(id: Long): User {
        return this.users
            .firstOrNull { it.id == id }
            ?: run { throw RuntimeException("User not found") }
    }
}