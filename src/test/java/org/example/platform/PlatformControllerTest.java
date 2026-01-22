package org.example.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.auth.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlatformController.class)
@WithMockUser
public class PlatformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlatformService platformService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("플랫폼 목록 조회 성공")
    void findAll() throws Exception {
        Platform platform = Platform.builder()
                .platformId(1L)
                .name("Test Platform")
                .build();

        given(platformService.findAll()).willReturn(List.of(platform));

        mockMvc.perform(get("/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Platform"));
    }

    @Test
    @DisplayName("플랫폼 단건 조회 성공")
    void findOne() throws Exception {
        Platform platform = Platform.builder()
                .platformId(1L)
                .name("Test Platform")
                .build();

        given(platformService.findById(1L)).willReturn(platform);

        mockMvc.perform(get("/platforms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Platform"));
    }

    @Test
    @DisplayName("플랫폼 생성 성공")
    void create() throws Exception {
        Platform platform = Platform.builder()
                .name("New Platform")
                .build();

        given(platformService.create(any())).willReturn(platform);

        mockMvc.perform(post("/platforms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(platform)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Platform"));
    }

    @Test
    @DisplayName("플랫폼 수정 성공")
    void update() throws Exception {
        PlatformUpdateRequestDto dto = new PlatformUpdateRequestDto();
        dto.setName("Updated Platform");

        Platform platform = Platform.builder()
                .name("Updated Platform")
                .build();

        given(platformService.update(anyLong(), any())).willReturn(platform);

        mockMvc.perform(put("/platforms/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Platform"));
    }

    @Test
    @DisplayName("플랫폼 비활성화 성공")
    void deactivate() throws Exception {
        mockMvc.perform(delete("/platforms/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
