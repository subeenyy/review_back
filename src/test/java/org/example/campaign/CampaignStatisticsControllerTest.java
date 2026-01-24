package org.example.campaign;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.auth.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
@WithMockUser
public class CampaignStatisticsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CampaignService campaignService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        private String token = "Bearer test-token";
        private Long userId = 1L;

        @BeforeEach
        void setUp() {
                given(jwtTokenProvider.getUserId(anyString())).willReturn(userId);
        }

        @Test
        @DisplayName("월별 통계 조회 성공")
        void getMonthlyStatisticsSuccess() throws Exception {
                // Given
                CampaignMonthlyStatisticsResponse response = CampaignMonthlyStatisticsResponse.builder()
                                .monthlyData(Collections.singletonList(
                                                CampaignMonthlyStatisticsResponse.MonthlyMetrics.builder()
                                                                .month("2024-01")
                                                                .totalCount(5)
                                                                .statusCount(Map.of("DONE", 3L, "VISITED", 2L))
                                                                .visitRate(1.0)
                                                                .reviewRate(0.6)
                                                                .totalSupportAmount(50000L)
                                                                .totalExtraCost(10000L)
                                                                .totalExpenditure(60000L)
                                                                .averageExpenditure(12000.0)
                                                                .build()))
                                .statusDistribution(Map.of("DONE", 3L, "VISITED", 2L))
                                .build();

                given(campaignService.getMonthlyStatistics(eq(userId), anyString(), anyString(), anyString(), any()))
                                .willReturn(response);

                // When & Then
                mockMvc.perform(get("/campaigns/statistics/monthly")
                                .header("Authorization", token)
                                .param("startMonth", "2024-01")
                                .param("endMonth", "2024-03")
                                .param("base", "deadline"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.monthlyData[0].month").value("2024-01"))
                                .andExpect(jsonPath("$.monthlyData[0].totalCount").value(5))
                                .andExpect(jsonPath("$.statusDistribution.DONE").value(3));
        }

        @Test
        @DisplayName("카테고리 필터링 포함 월별 통계 조회 성공")
        void getMonthlyStatisticsWithCategorySuccess() throws Exception {
                // Given
                CampaignMonthlyStatisticsResponse response = CampaignMonthlyStatisticsResponse.builder()
                                .monthlyData(Collections.emptyList())
                                .statusDistribution(Collections.emptyMap())
                                .build();

                given(campaignService.getMonthlyStatistics(eq(userId), anyString(), anyString(), anyString(), eq(1L)))
                                .willReturn(response);

                // When & Then
                mockMvc.perform(get("/campaigns/statistics/monthly")
                                .header("Authorization", token)
                                .param("startMonth", "2024-01")
                                .param("endMonth", "2024-03")
                                .param("base", "deadline")
                                .param("categoryId", "1"))
                                .andExpect(status().isOk());
        }
}
