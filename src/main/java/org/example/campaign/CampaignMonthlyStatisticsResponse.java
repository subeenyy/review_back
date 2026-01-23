package org.example.campaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignMonthlyStatisticsResponse {
    private List<MonthlyMetrics> monthlyData;
    private Map<String, Long> statusDistribution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyMetrics {
        private String month; // YYYY-MM
        private long totalCount;
        private Map<String, Long> statusCount;
        private double visitRate; // VISITED / RESERVED
        private long reviewCount; // receiptReview=true & status=DONE
        private double reviewRate; // reviewCount / DONE
        private long totalSupportAmount;
        private long totalExtraCost;
        private long totalExpenditure;
        private double averageExpenditure;
    }
}
