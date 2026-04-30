package com.futureschole.liveclass.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"-1", "Server Error Occurred"),
    BAD_REQUEST_FIELD_VALID_ERROR(HttpStatus.BAD_REQUEST,"-2", "Bad Request Field Error Occurred"),


    NOT_FOUND_COURSE(HttpStatus.NOT_FOUND, "-100", "Course is not exists")
}