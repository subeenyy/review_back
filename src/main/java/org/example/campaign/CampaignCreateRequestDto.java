package org.example.campaign;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class CampaignCreateRequestDto {

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

}
