package com.futureschole.liveclass.domain.user.entity

class User(
    val id: Long,
    val role: UserRole
)

enum class UserRole {
    USER,
    ADMIN
}