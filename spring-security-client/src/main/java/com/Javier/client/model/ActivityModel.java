package com.Javier.client.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityModel {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Long activityId;
    private String activityName;
    private LocalDateTime timestamp;
}
