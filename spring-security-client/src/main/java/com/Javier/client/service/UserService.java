package com.Javier.client.service;

import com.Javier.client.entity.User;
import com.Javier.client.entity.VerificationToken;
import com.Javier.client.model.UserModel;
import com.Javier.client.model.ActivityModel;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);

    List<UserModel> getData(UserModel userModel);

    List<ActivityModel> getActivities(UserModel userModel);

    void recordActivity(String activityName, User user);

    void deleteUser(User user);
}
