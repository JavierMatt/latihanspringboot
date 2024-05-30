package com.Javier.client.controller;

import com.Javier.client.entity.User;
import com.Javier.client.model.LoginModel;
import com.Javier.client.model.ResponseModel;
import com.Javier.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ResponseModel> login(@RequestBody LoginModel loginRequest) {
        User user = userService.findUserByEmail(loginRequest.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseModel(401, "Invalid email or password", null,null));
        }
        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseModel(401, "User account is deleted", null, null));
        }

        // cek password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseModel(401, "Invalid email or password", null,null));
        }
        userService.recordActivity("User Login", user);
        // masuk
        return ResponseEntity.ok()
                .body(new ResponseModel(200, "Login successful", null,null));
    }
}
