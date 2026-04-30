package com.futureschole.liveclass.common.exception

data class ExceptionResponse(
    val errorCode: String,
    val message: String,
    val extra: Map<String, String>? = null
) {
    constructor(errorCode: ErrorCode, extra: Map<String, String>? = null) : this(
        errorCode = errorCode.code,
        message = errorCode.message,
        extra = extra
    )
}