package org.example.platform;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlatformUpdateRequestDto {
    private String name;
    private Boolean rewardEnabled;
    private Long rewardPolicyId;
    private Boolean active;
}
