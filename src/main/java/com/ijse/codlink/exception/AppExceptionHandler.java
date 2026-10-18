package com.ijse.codlink.exception;

import com.ijse.codlink.responce.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {CustomException.class})
    public ResponseEntity<CommonResponse> handleCustomException(CustomException exception) {
        log.warn("Handled exception: {}",exception.getMessage() );


        return ResponseEntity.status(exception.getStatus())
                .body(new CommonResponse(exception.getStatus() ,exception.getMessage()));
    }

    @ExceptionHandler({org.springframework.dao.DataIntegrityViolationException.class,
            org.springframework.dao.ConcurrencyFailureException.class})
    public ResponseEntity<CommonResponse> handleConflict(Exception exception) {
        return ResponseEntity.status(409).body(new CommonResponse(409, "Record changed or already exists. Refresh and retry."));
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<CommonResponse> handlerServerException(Exception exception , WebRequest webRequest){
        log.error("Unhandled exception: {}",exception.getMessage() );
        return ResponseEntity.status(500).body(new CommonResponse(500, "Something went wrong"));
    }



    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String messsage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Validation Failed");

        log.warn("Validation Failed: {}", messsage );
        return ResponseEntity.status(400).body(new CommonResponse(400, messsage));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.status(400).body(new CommonResponse(400, "Malformed request body"));
    }
}

