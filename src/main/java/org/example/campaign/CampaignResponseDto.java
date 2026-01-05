package org.example.campaign;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CampaignResponseDto {
    private Long id;
    private String storeName;
    private String storePhone;
    private String address;
    private Long supportAmount;
    private Long extraCost;
    private Boolean receiptReview;
    private LocalDate experienceStartDate;
    private LocalDate experienceEndDate;
    private LocalDate deadline;
    private List<String> availableDays;
    private String availableTime;
    private String status;

    public static CampaignResponseDto fromEntity(Campaign s) {
        List<String> avaliavleDaysList = s.getAvailableDays() == null
                ? Collections.emptyList()
                : Arrays.asList(s.getAvailableDays().split(","));
        return new CampaignResponseDto(
                s.getCampaignId(),
                s.getStoreName(),
                s.getStorePhone(),
                s.getAddress(),
                s.getSupportAmount(),
                s.getExtraCost(),
                s.getReceiptReview(),
                s.getExperienceStartDate(),
                s.getExperienceEndDate(),
                s.getDeadline(),
                avaliavleDaysList,   // List<String>로 변환되어 있다고 가정
                s.getAvailableTime(),
                s.getStatus().name()    // Enum이면 name()으로 문자열 변환
        );
    }

}
