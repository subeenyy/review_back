package org.example.campaign;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.example.common.config.TestCacheConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestCacheConfig.class)
public class RedisCacheIntegrationTest {

    @Autowired
    private CampaignService campaignService;

    @MockBean
    private CampaignRepository campaignRepository;

    @Test
    @DisplayName("findAllByUserId - 캐시 적용 시 DB 조회 1회 수행 검증 (HIT)")
    void findAllByUserId_Cache_Verification() {
        // Given
        Long userId = 999L;
        Campaign mockCampaign = new Campaign(); // Needs basic fields if accessed, but list returns empty is fine
        when(campaignRepository.findByUserId(userId)).thenReturn(List.of(mockCampaign));

        // When 1: First Call (Cache MISS)
        System.out.println(">>> 1st Call (Should be MISS)");
        List<Campaign> result1 = campaignService.findAllByUserId(userId);

        // When 2: Second Call (Cache HIT)
        System.out.println(">>> 2nd Call (Should be HIT)");
        List<Campaign> result2 = campaignService.findAllByUserId(userId);

        // Then
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1); // Content should be same

        // Repository should be called ONLY ONCE
        verify(campaignRepository, times(1)).findByUserId(userId);

        System.out.println(">>> Verification Passed: Repository called 1 time for 2 service requests.");
    }
}
