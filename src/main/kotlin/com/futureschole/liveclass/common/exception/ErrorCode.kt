package com.futureschole.liveclass.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"-1", "Server Error Occurred"),
    BAD_REQUEST_FIELD_VALID_ERROR(HttpStatus.BAD_REQUEST,"-2", "Bad Request Field Error Occurred"),
    BAD_REQUEST_BODY(HttpStatus.BAD_REQUEST, "-3", "Bad Request Body Error Occurred"),
    BAD_REQUEST_QUERY_PARAM(HttpStatus.BAD_REQUEST, "-4", "Bad Request Query Param Error Occurred"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "-10", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "-11", "Access denied"),

    NOT_FOUND_COURSE(HttpStatus.NOT_FOUND, "-100", "Course is not exists"),
    NOT_FOUND_SALE_RECORD(HttpStatus.NOT_FOUND, "-101", "Sale record is not exists"),
    INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "-102", "Cancel amount exceeds sale record amount"),
    INVALID_CANCEL_AT_NOT_AFTER_PAID_AT(HttpStatus.BAD_REQUEST, "-103", "Cancel at must be after paid at"),
    INVALID_CANCEL_AT_IN_FUTURE(HttpStatus.BAD_REQUEST, "-104", "Cancel at must not be in the future"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "-105", "Start date must not be after end date"),

    DUPLICATE_SETTLEMENT(HttpStatus.BAD_REQUEST, "-200", "Settlement already exists"),
    INVALID_SETTLEMENT_MONTH_NOT_CLOSED(HttpStatus.BAD_REQUEST, "-201", "Settlement month must be before current month"),
    INVALID_SETTLEMENT_MONTH_ORDER(HttpStatus.BAD_REQUEST, "-202", "Settlement month must be next month after latest settlement")
}
