package org.example.campaign;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String storeName;
    private String storePhone;
    private String address;
    private Long platformId;
    private String platformName;
    private Long categoryId;
    private String categoryName;
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
                s.getPlatform().getPlatformId(),
                s.getPlatform().getName(),
                s.getCategory() != null ? s.getCategory().getId() : null,
                s.getCategory() != null ? s.getCategory().getName() : null,
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
