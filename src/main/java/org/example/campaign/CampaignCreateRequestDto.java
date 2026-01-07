package org.example.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class CampaignCreateRequestDto {

    /** 플랫폼 식별자 (리뷰노트, 자사 플랫폼 등) */
    @NotNull
    private Long platformId;

    /** 가게 정보 */
    @NotBlank
    private String storeName;

    @NotBlank
    private String storePhone;

    @NotBlank
    private String address;

    /** 지원금 / 추가금 */
    @NotNull
    private Long supportAmount;

    private Long extraCost;

    /** 영수증 리뷰 여부 */
    @NotNull
    private Boolean receiptReview;

    /** 체험 기간 */
    @NotNull
    private LocalDate experienceStartDate;

    @NotNull
    private LocalDate experienceEndDate;

    /** 리뷰 마감일 */
    @NotNull
    private LocalDate deadline;

    /** 방문 가능 요일 */
    @NotEmpty
    private List<String> availableDays;

    /** 방문 가능 시간대 */
    @NotBlank
    private String availableTime;
}
