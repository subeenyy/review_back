package org.example.platform;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "platform")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long platformId;

    @Column(nullable = false, unique = true)
    private String code; // review_note, dinner_queen, our_platform

    @Column(nullable = false)
    private String name; // 리뷰노트, 디너의여왕, 우리플랫폼

    @Column(nullable = false)
    private boolean rewardEnabled;

    @Column(nullable = false)
    private boolean active;

    private Long rewardPolicyId;
}
