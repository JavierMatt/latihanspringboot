package com.Javier.client.handler;

import com.Javier.client.exception.InvalidPasswordException;
import com.Javier.client.model.ResponseModel;
import com.Javier.client.model.UserModel;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class UserServiceExceptionHandler {

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ResponseModel> handleInvalidPasswordException(InvalidPasswordException e) {
        log.info("Handler InvalidPasswordException: {}", e.getMessage());

        ResponseModel responseModel = new ResponseModel(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                e.getResponseObj(),
                e.getUsers()
        );

        return new ResponseEntity<>(responseModel, HttpStatus.BAD_REQUEST);
    }
}
