package com.Javier.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseModel {
    private Integer responseCode;
    private String responseMessage;
    private UserModel responseObj;
    private List<UserModel> users;
}
