package org.example.sponsorship;

import jakarta.persistence.*;
import lombok.*;
import org.example.user.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "sponsorship")
@Getter
@Setter
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

    @Column(name = "available_days", length = 50)
    private String availableDays;


    private String availableTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    // 상태 변경 메서드
    public void reserve() { if (status != Status.PENDING) throw new IllegalStateException("PENDING 상태에서만 예약 가능"); status = Status.RESERVED; }
    public void visit() { if (status != Status.RESERVED) throw new IllegalStateException("RESERVED 상태에서만 방문 처리 가능"); status = Status.VISITED; }
    public void complete() { if (status != Status.VISITED) throw new IllegalStateException("VISITED 상태에서만 완료 처리 가능"); status = Status.DONE; }
    public void cancel() { status = Status.CANCELED; }


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
            String availableDays,
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

    // Entity → DTO 변환
    public Set<DayOfWeek> getAvailableDaysAsSet(com.fasterxml.jackson.databind.ObjectMapper objectMapper) throws com.fasterxml.jackson.core.JsonProcessingException {
        if (this.availableDays == null) return Set.of();
        return objectMapper.readValue(this.availableDays, new com.fasterxml.jackson.core.type.TypeReference<Set<DayOfWeek>>() {});
    }

    /* 팩토리 메서드 */
    public static Sponsorship create(User user, SponsorshipCreateRequestDto req){
        String csvDays = String.join(",", req.getAvailableDays());
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
                .availableDays(csvDays)
                .availableTime(req.getAvailableTime())
                .status(Status.PENDING)
                .build();
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    private String toCsv(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return "";
        return days.stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(","));
    }

    public List<String> getAvailableDaysList() {
        if (availableDays == null || availableDays.isEmpty()) return List.of();
        return List.of(availableDays.split(","));
    }
}

