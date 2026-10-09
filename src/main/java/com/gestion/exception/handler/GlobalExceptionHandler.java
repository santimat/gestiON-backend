package com.gestion.exception.handler;

import com.gestion.dto.response.exception.ExceptionResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleBusinessException(BusinessException exception) {
        return buildResponse(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(AuthenticationException exception) {
        return buildResponse(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getDefaultMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return buildResponse(ErrorCode.ACCESS_DENIED, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException exception) {
        // mapea todos los errores de validación a una lista de FieldError(campo,mensaje de error) para que sea más facil de manejar en el front-end
        return buildValidationResponse(exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ExceptionResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("Malformed request body: {}", exception.getMessage());
        return buildResponse(ErrorCode.INVALID_REQUEST_BODY, ErrorCode.INVALID_REQUEST_BODY.getDefaultMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return buildResponse(ErrorCode.INVALID_PARAMETER,
                "Parameter '" + exception.getName() + "' is invalid");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return buildResponse(ErrorCode.MISSING_PARAMETER,
                "Parameter '" + exception.getParameterName() + "' is required");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getDefaultMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        return buildResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getDefaultMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        return buildResponse(ErrorCode.ENDPOINT_NOT_FOUND, ErrorCode.ENDPOINT_NOT_FOUND.getDefaultMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        log.warn("Upload size exceeded: {}", exception.getMessage());
        return buildResponse(ErrorCode.FILE_TOO_LARGE, ErrorCode.FILE_TOO_LARGE.getDefaultMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error("Data integrity violation: {}", exception.getMostSpecificCause().getMessage(), exception);
        return buildResponse(ErrorCode.DATA_INTEGRITY_VIOLATION, ErrorCode.DATA_INTEGRITY_VIOLATION.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        log.error("Unhandled exception: {}", exception.getMessage(), exception);
        return buildResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    // Los errores normales y los de validación se manejan de manera diferente debido a su construcción
    private ResponseEntity<ExceptionResponse> buildValidationResponse(List<ExceptionResponse.FieldError> fields) {
        log.warn("Request validation failed: {}", fields);
        ExceptionResponse body = new ExceptionResponse(
                ErrorCode.VALIDATION_ERROR.getStatus(),
                ErrorCode.VALIDATION_ERROR.name(),
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                fields
        );
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(body);
    }

    private ResponseEntity<ExceptionResponse> buildResponse(ErrorCode code, String message) {
        // A no ser que se sobreescriba el mensaje se utiliza el mensaje por defecto del código de error, el cual se define en el enum ErrorCode
        String responseMessage = (message == null || message.isBlank()) ? code.getDefaultMessage() : message;

        // si es un error del server se guardar el log como error, si es un error del cliente se guarda como warning
        if (code.getStatus() >= 500) log.error("{}: {}", code.name(), responseMessage);
        else log.warn("{}: {}", code.name(), responseMessage);

        // se parsea a un dto de respuesta de error para que sea más facil de manejar en el front-end y se devuelve con el status correspondiente
        ExceptionResponse body = new ExceptionResponse(code.getStatus(), code.name(), responseMessage);
        return ResponseEntity.status(code.getStatus()).body(body);
    }
}
