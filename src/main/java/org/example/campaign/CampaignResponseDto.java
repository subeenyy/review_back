package org.example.campaign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDate visitDate;
    private List<String> availableDays;
    private String availableTime;
    private String status;

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
                s.getVisitDate(),
                availableDaysList,
                s.getAvailableTime(),
                s.getStatus().name());
    }
}
