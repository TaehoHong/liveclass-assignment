package com.futureschole.liveclass.domain.course.entity

import jakarta.persistence.*

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