package com.Javier.client.service;

import com.Javier.client.entity.Activity;
import com.Javier.client.entity.PasswordResetToken;
import com.Javier.client.entity.User;
import com.Javier.client.entity.VerificationToken;
import com.Javier.client.exception.InvalidPasswordException;
import com.Javier.client.exception.RegisterException;
import com.Javier.client.model.ActivityModel;
import com.Javier.client.model.UserModel;
import com.Javier.client.repository.ActivityRepository;
import com.Javier.client.repository.PasswordResetTokenRepository;
import com.Javier.client.repository.UserRepository;
import com.Javier.client.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivityRepository activityRepository;

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    private final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    private void validatePassword(String password, UserModel userModel) {
        StringBuilder errorMessage = new StringBuilder("Password must have:");
        if (!pattern.matcher(password).matches()) {
            if (!password.matches(".*[A-Z].*")) {
                errorMessage.append("\n- Minimal 1 kapital (A-Z)");
            }
            if (!password.matches(".*[0-9].*")) {
                errorMessage.append("\n- Minimal 1 digit (0-9)");
            }
            if (!password.matches(".*[@#$%^&+=].*")) {
                errorMessage.append("\n- Minimal 1 special character (@#$%^&+=)");
            }
            if (password.length() < 8) {
                errorMessage.append("\n- Minimal 8 kata");
            }
            throw new InvalidPasswordException(errorMessage.toString(), userModel);
        }
    }


    @Override
    public User registerUser(UserModel userModel) {
        validatePassword(userModel.getPassword(), userModel);

        String email = userModel.getEmail();
        if (email == null || email.isEmpty() || email.length() < 3) {
            throw new RegisterException("Email must be filled and have a minimum length of 3 characters");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RegisterException("Email is already registered");
        }

        User user = new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        user.setJenisKelamin(userModel.getJenisKelamin());
        userRepository.save(user);
        recordActivity("User registered", user);
        return user;
    }
    public List<UserModel> getData(UserModel userModel) {
        List<User> users = userRepository.findAll();
        //firstname - Javier maka akan cari semua yang ada Javier (semua yg ada di usermodel) , kalo kosong dia tampilin smua
        return users.stream()
                .filter(user -> !user.isDeleted())
                .filter(user -> (userModel.getFirstName() == null || userModel.getFirstName().isEmpty() || user.getFirstName().contains(userModel.getFirstName()))
                        && (userModel.getLastName() == null || userModel.getLastName().isEmpty() || user.getLastName().contains(userModel.getLastName()))
                        && (userModel.getEmail() == null || userModel.getEmail().isEmpty() || user.getEmail().contains(userModel.getEmail())))
                .map(user -> new UserModel(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        null,
                        null,
                        user.getJenisKelamin()
                ))
                .collect(Collectors.toList());
    }

    public void recordActivity(String activityName, User user) {
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setTimestamp(LocalDateTime.now());
        activity.setActivityName(activityName);
        activityRepository.save(activity);
    }

    @Override
    public List<ActivityModel> getActivities(UserModel userModel) {
        List<Activity> activities = activityRepository.findAll();

        // mirip dengan getdata tapi ada user dan acitivty
        // bingung : cara gabungin 2 tabel sebagai response model, yang sekarang cuma masukin 2 tabel ke 1 model bukan join
        return activities.stream()
                .filter(activity -> (userModel.getEmail() == null || userModel.getEmail().isEmpty() || activity.getUser().getEmail().contains(userModel.getEmail())))
                .map(activity -> {
                    ActivityModel activityModel = new ActivityModel();
                    activityModel.setActivityId(activity.getId());
                    activityModel.setUserId(activity.getUser().getId());
                    activityModel.setFirstName(activity.getUser().getFirstName());
                    activityModel.setLastName(activity.getUser().getLastName());
                    activityModel.setEmail(activity.getUser().getEmail());
                    activityModel.setTimestamp(activity.getTimestamp());
                    activityModel.setActivityName(activity.getActivityName());
                    return activityModel;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(User user) {
        user.setDeleted(true);
        userRepository.save(user);
        recordActivity("Delete user", user);
    }


    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken
                = new VerificationToken(user, token);

        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken
                = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return "invalid";
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();

        if ((verificationToken.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }

        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken
                = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken
                = new PasswordResetToken(user,token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken
                = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken == null) {
            return "invalid";
        }

        User user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();

        if ((passwordResetToken.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        recordActivity("Change Password", user);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
