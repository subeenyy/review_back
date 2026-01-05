package org.example.reward;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.campaign.Campaign;
import org.example.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "reward")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rewardId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Campaign campaign;

    private Long amount;  // 지급 금액

    private LocalDateTime issuedAt;

    private Boolean issued = true;  // 지급 여부
}
