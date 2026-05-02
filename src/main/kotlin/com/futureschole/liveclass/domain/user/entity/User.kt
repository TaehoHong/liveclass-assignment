package com.futureschole.liveclass.domain.user.entity

class User(
    val id: Long,
    val role: UserRole,
    val creatorId: Long? = null
)

enum class UserRole {
    CREATOR,
    ADMIN
}
