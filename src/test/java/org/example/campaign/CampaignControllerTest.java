package org.example.campaign;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.auth.JwtTokenProvider;
import org.example.category.Category;
import org.example.platform.Platform;
import org.example.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
@WithMockUser
public class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CampaignService campaignService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private Long userId = 1L;
    private String token = "Bearer test-token";
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.getUserId(anyString())).willReturn(userId);

        User user = User.builder().id(userId).build();
        Platform platform = Platform.builder().platformId(1L).name("Test Platform").build();
        Category category = Category.builder().id(1L).name("Test Category").build();

        campaign = Campaign.builder()
                .id(1L)
                .user(user)
                .platform(platform)
                .category(category)
                .storeName("Test Store")
                .storePhone("010-1234-5678")
                .address("Test Address")
                .receiptReview(true)
                .status(Status.PENDING)
                .supportAmount(10000L)
                .availableDays("MONDAY,TUESDAY")
                .experienceStartDate(LocalDate.now())
                .experienceEndDate(LocalDate.now().plusDays(7))
                .deadline(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    @DisplayName("캠페인 단건 조회 성공")
    void getCampaignById() throws Exception {
        given(campaignService.findByCampaignIdAndUser(anyLong(), anyLong()))
                .willReturn(Optional.of(campaign));

        mockMvc.perform(get("/campaigns/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Test Store"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("캠페인 생성 성공")
    void createCampaign() throws Exception {
        CampaignCreateRequestDto requestDto = new CampaignCreateRequestDto();
        requestDto.setStoreName("New Store");
        requestDto.setStorePhone("010-1111-2222");
        requestDto.setAddress("New Address");
        requestDto.setReceiptReview(true);
        requestDto.setAvailableTime("14:00 - 18:00");
        requestDto.setPlatformId(1L);
        requestDto.setCategoryId(1L);
        requestDto.setAvailableDays(List.of("MONDAY", "TUESDAY"));
        requestDto.setExperienceStartDate(LocalDate.now());
        requestDto.setExperienceEndDate(LocalDate.now().plusDays(7));
        requestDto.setDeadline(LocalDate.now().plusDays(5));
        requestDto.setSupportAmount(20000L);

        given(campaignService.createCampaign(anyLong(), any(CampaignCreateRequestDto.class)))
                .willReturn(campaign);

        mockMvc.perform(post("/campaigns")
                .header("Authorization", token)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Test Store"));
    }

    @Test
    @DisplayName("사용자 캠페인 목록 조회")
    void getCampaigns() throws Exception {
        given(campaignService.findAllByUserId(anyLong())).willReturn(List.of(campaign));

        mockMvc.perform(get("/campaigns")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].storeName").value("Test Store"));
    }

    @Test
    @DisplayName("캠페인 수정 성공")
    void updateCampaign() throws Exception {
        CampaignResponseDto updateDto = CampaignResponseDto.fromEntity(campaign);
        updateDto.setStoreName("Updated Store");

        given(campaignService.updateCampaign(anyLong(), anyLong(), any(CampaignResponseDto.class)))
                .willReturn(campaign);

        mockMvc.perform(patch("/campaigns/1")
                .header("Authorization", token)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("캠페인 삭제 성공")
    void deleteCampaign() throws Exception {
        mockMvc.perform(delete("/campaigns/1")
                .header("Authorization", token)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(campaignService).deleteCampaign(anyLong(), anyLong());
    }

    @Test
    @DisplayName("캠페인 상태 변경 성공")
    void changeStatus() throws Exception {
        mockMvc.perform(patch("/campaigns/1/status/RESERVE")
                .header("Authorization", token)
                .with(csrf())
                .param("visitDate", LocalDate.now().toString()))
                .andExpect(status().isOk());

        verify(campaignService).changeStatus(anyLong(), anyLong(), eq(CampaignAction.RESERVE), any(LocalDate.class));
    }

    @Test
    @DisplayName("상태별 캠페인 목록 조회")
    void getCampaignsByStatus() throws Exception {
        given(campaignService.findCampaigns(anyLong(), any(), any()))
                .willReturn(List.of(CampaignResponseDto.fromEntity(campaign)));

        mockMvc.perform(get("/campaigns/status")
                .header("Authorization", token)
                .param("status", "PENDING")
                .param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
}
