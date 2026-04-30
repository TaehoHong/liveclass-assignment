package com.futureschole.liveclass.domain.course.repository

import com.futureschole.liveclass.domain.course.entity.Course
import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository: JpaRepository<Course, Long> {
}