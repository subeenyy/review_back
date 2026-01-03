package org.example.sponsorship;


import jakarta.persistence.*;
import lombok.*;
import org.example.user.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sponsorship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String storeName;
    private String storePhone;
    private String address;

    private Long supportAmount;
    private Long extraCost;
    private Boolean receiptReview;

    private LocalDate experienceStartDate;
    private LocalDate experienceEndDate;
    private LocalDate deadline;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availableDays;

    private String availableTime;

    @Enumerated(EnumType.STRING)
    private Status status;
    // 지원 → 예약
    public void reserve() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 예약 가능");
        }
        this.status = Status.RESERVED;
    }

    // 예약 → 방문
    public void visit() {
        if (this.status != Status.RESERVED) {
            throw new IllegalStateException("RESERVED 상태에서만 방문 처리 가능");
        }
        this.status = Status.VISITED;
    }

    // 방문 → 완료
    public void complete() {
        if (this.status != Status.VISITED) {
            throw new IllegalStateException("VISITED 상태에서만 완료 처리 가능");
        }
        this.status = Status.DONE;
    }

    // 언제든 취소 가능 (완료 제외)
    public void cancel() {
        if (this.status == Status.DONE) {
            throw new IllegalStateException("완료된 협찬은 취소 불가");
        }
        this.status = Status.CANCELED;
    }

    @Builder
    private Sponsorship(
            User user,
            String storeName,
            String storePhone,
            String address,
            Long supportAmount,
            Long extraCost,
            Boolean receiptReview,
            LocalDate experienceStartDate,
            LocalDate experienceEndDate,
            LocalDate deadline,
            Set<DayOfWeek> availableDays,
            String availableTime,
            Status status
    ) {
        this.user = user;
        this.storeName = storeName;
        this.storePhone = storePhone;
        this.address = address;
        this.supportAmount = supportAmount;
        this.extraCost = extraCost;
        this.receiptReview = receiptReview;
        this.experienceStartDate = experienceStartDate;
        this.experienceEndDate = experienceEndDate;
        this.deadline = deadline;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
        this.status = status;
    }

    /* 팩토리 메서드 */
    public static Sponsorship create(User user, SponsorshipCreateRequestDto req) {
        return Sponsorship.builder()
                .user(user)
                .storeName(req.getStoreName())
                .storePhone(req.getStorePhone())
                .address(req.getAddress())
                .supportAmount(req.getSupportAmount())
                .extraCost(req.getExtraCost())
                .receiptReview(req.getReceiptReview())
                .experienceStartDate(req.getExperienceStartDate())
                .experienceEndDate(req.getExperienceEndDate())
                .deadline(req.getDeadline())
                .availableDays(req.getAvailableDays())
                .availableTime(req.getAvailableTime())
                .status(Status.PENDING) // ✅ 규칙 고정
                .build();
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

}

