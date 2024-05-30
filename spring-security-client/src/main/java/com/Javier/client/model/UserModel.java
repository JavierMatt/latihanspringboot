package com.Javier.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String matchingPassword;
    private String jenisKelamin;
    private boolean deleted;

    public UserModel(String firstName, String lastName, String email, String password, String matchingPassword, String jenisKelamin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.matchingPassword = matchingPassword;
        this.jenisKelamin = jenisKelamin;
    }
}
