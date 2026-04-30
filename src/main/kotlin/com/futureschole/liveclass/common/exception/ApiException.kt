package com.futureschole.liveclass.common.exception

class ApiException(
    val errorCode: ErrorCode,
    override val message: String? = null,
): RuntimeException(message?:errorCode.message) {
}