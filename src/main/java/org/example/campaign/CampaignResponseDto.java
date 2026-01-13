package org.example.campaign;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class CampaignResponseDto {

    private Long id;
    private String storeName;
    private String storePhone;
    private String address;
    private String platformName; // 추가
    private Long supportAmount;
    private Long extraCost;
    private boolean receiptReview;
    private LocalDate experienceStartDate;
    private LocalDate experienceEndDate;
    private LocalDate deadline;
    private List<String> availableDays;
    private String availableTime;
    private String status;

    public CampaignResponseDto(
            Long id,
            String storeName,
            String storePhone,
            String address,
            String platformName,
            Long supportAmount,
            Long extraCost,
            boolean receiptReview,
            LocalDate experienceStartDate,
            LocalDate experienceEndDate,
            LocalDate deadline,
            List<String> availableDays,
            String availableTime,
            String status
    ) {
        this.id = id;
        this.storeName = storeName;
        this.storePhone = storePhone;
        this.address = address;
        this.platformName = platformName;
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

    public static CampaignResponseDto fromEntity(Campaign s) {
        List<String> availableDaysList = s.getAvailableDays() == null
                ? Collections.emptyList()
                : Arrays.asList(s.getAvailableDays().split(","));

        return new CampaignResponseDto(
                s.getId(),
                s.getStoreName(),
                s.getStorePhone(),
                s.getAddress(),
                s.getPlatform().getName(), // platformName
                s.getSupportAmount(),
                s.getExtraCost(),
                s.getReceiptReview(),
                s.getExperienceStartDate(),
                s.getExperienceEndDate(),
                s.getDeadline(),
                availableDaysList,
                s.getAvailableTime(),
                s.getStatus().name()
        );
    }
}
