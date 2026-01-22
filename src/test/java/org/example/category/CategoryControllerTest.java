package org.example.category;

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

@WebMvcTest(CategoryController.class)
@WithMockUser
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("활성 카테고리 목록 조회 성공")
    void getCategories() throws Exception {
        CategoryResponseDto category = CategoryResponseDto.builder()
                .id(1L)
                .name("Test Category")
                .displayOrder(1)
                .isActive("1")
                .build();

        given(categoryService.getActiveCategories()).willReturn(List.of(category));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Category"));
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory() throws Exception {
        CategoryResponseDto dto = CategoryResponseDto.builder()
                .name("New Category")
                .displayOrder(2)
                .build();

        given(categoryService.createCategory(any())).willReturn(dto);

        mockMvc.perform(post("/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory() throws Exception {
        CategoryResponseDto dto = CategoryResponseDto.builder()
                .name("Updated Category")
                .build();

        given(categoryService.updateCategory(anyLong(), any())).willReturn(dto);

        mockMvc.perform(patch("/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }
}
