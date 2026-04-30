package com.futureschole.liveclass.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CustomControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }

        return ResponseEntity(
            ExceptionResponse(ErrorCode.BAD_REQUEST_FIELD_VALID_ERROR, errors),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ExceptionResponse> {
        ex.printStackTrace()

        return ResponseEntity(
            ExceptionResponse(ErrorCode.BAD_REQUEST_BODY),
            ErrorCode.BAD_REQUEST_BODY.httpStatus
        )
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ExceptionResponse> {

        ex.printStackTrace()

        return Triple(
            ex.errorCode.code,
            ex.message?:ex.errorCode.message,
            ex.errorCode.httpStatus
        ).let { (errorCode, message, status) ->
            ResponseEntity(
                ExceptionResponse(errorCode, message),
                status
            )
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ExceptionResponse> {

        ex.printStackTrace()

        return ResponseEntity(
            ExceptionResponse(ErrorCode.SERVER_ERROR),
            ErrorCode.SERVER_ERROR.httpStatus
        )
    }
}
