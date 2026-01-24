package org.example.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsDto {
    private boolean notificationEnabled;
    private Integer notificationHour;
}
