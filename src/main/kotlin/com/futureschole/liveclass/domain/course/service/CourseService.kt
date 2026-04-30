package com.futureschole.liveclass.domain.course.service

import com.futureschole.liveclass.common.exception.ApiException
import com.futureschole.liveclass.common.exception.ErrorCode
import com.futureschole.liveclass.domain.course.entity.Course
import com.futureschole.liveclass.domain.course.repository.CourseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService(
    private val courseRepository: CourseRepository
) {

    @Transactional(readOnly = true)
    fun getById(id: Long): Course {
        return courseRepository.findById(id)
            .orElseThrow { ApiException(ErrorCode.NOT_FOUND_COURSE) }
    }
}