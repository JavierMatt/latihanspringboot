package com.Javier.client.controller;

import com.Javier.client.entity.User;
import com.Javier.client.entity.VerificationToken;
import com.Javier.client.event.RegistrationCompleteEvent;
import com.Javier.client.exception.RegisterException;
import com.Javier.client.model.*;
import com.Javier.client.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseModel registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        try {
            userModel.setDeleted(false);
            User user = userService.registerUser(userModel);
            publisher.publishEvent(new RegistrationCompleteEvent(
                    user,
                    applicationUrl(request)
            ));
            ResponseModel response = new ResponseModel();
            response.setResponseCode(200);
            response.setResponseMessage("success");
            response.setResponseObj(userModel);
            response.setUsers(null);
            return response;
        } catch (RegisterException e) {
            throw e;
        }
    }
    @PostMapping("/getData")
    public ResponseModel getData(@RequestBody UserModel userModel) {
        List<UserModel> users = userService.getData(userModel);
        ResponseModel response = new ResponseModel();
        response.setResponseCode(200);
        response.setResponseMessage("success");
        response.setResponseObj(userModel);
        response.setUsers(users);
        return response;
    }

    @PostMapping("/getActivities")
    public ResponseEntity<List<ActivityModel>> getActivities(@RequestBody UserModel userModel) {
        List<ActivityModel> activities = userService.getActivities(userModel);
        return ResponseEntity.ok().body(activities);
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseModel> deleteUser(@RequestBody LoginModel deleteRequest) {
        User user = userService.findUserByEmail(deleteRequest.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseModel(401, "Invalid email", null, null));
        }
        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseModel(401, "User account is already deleted", null, null));
        }

        // cek password
        if (!passwordEncoder.matches(deleteRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseModel(401, "Invalid password", null, null));
        }
        userService.deleteUser(user);
        return ResponseEntity.ok()
                .body(new ResponseModel(200, "User deleted successfully", null, null));
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")) {
            return "User Verified Successfully";
        }
        return "Bad User";
    }


    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {
        VerificationToken verificationToken
                = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        String url = "";
        if(user!=null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url = passwordResetTokenMail(user,applicationUrl(request), token);
        }
        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel) {
        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")) {
            return "Invalid Token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()) {
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password Reset Successfully";
        } else {
            return "Invalid Token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if(!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save New Password
        userService.changePassword(user,passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url =
                applicationUrl
                        + "/savePassword?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to Reset your Password: {}",
                url);
        return url;
    }


    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url =
                applicationUrl
                        + "/verifyRegistration?token="
                        + verificationToken.getToken();

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }


    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }
}
