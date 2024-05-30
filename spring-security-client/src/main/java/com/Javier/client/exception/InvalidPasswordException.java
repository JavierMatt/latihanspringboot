package com.Javier.client.exception;

import com.Javier.client.model.UserModel;
import lombok.Getter;

import javax.annotation.processing.Generated;
import java.util.List;

@Getter
public class InvalidPasswordException extends RuntimeException {
    private final UserModel responseObj;
    private List<UserModel> users;
    public InvalidPasswordException(String message, UserModel responseObj) {
        super(message);
        this.responseObj = responseObj;
        this.users = users;
    }

    public UserModel getResponseObj() {
        return responseObj;
    }
}
