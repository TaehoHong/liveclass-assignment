package com.futureschole.liveclass.common.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "course")
@Entity
class Course(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "creator_id")
    val creatorId: Long,

    @Column(name = "title")
    val title: String
)
