package com.loopers.interfaces.api.brand.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.user.UserService;
import com.loopers.interfaces.api.auth.AdminAuthInterceptor;
import com.loopers.interfaces.api.auth.LoginUserArgumentResolver;
import com.loopers.interfaces.api.brand.admin.request.BrandCreateRequest;
import com.loopers.interfaces.api.brand.admin.request.BrandUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrandAdminV1Controller.class)
@Import({AdminAuthInterceptor.class, LoginUserArgumentResolver.class})
class BrandAdminV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private BrandFacade brandFacade;

    @MockitoBean
    private UserService userService;

    private static final String LDAP_HEADER = "X-Loopers-Ldap";
    private static final String VALID_LDAP = "test";

    @Test
    void 브랜드_등록_시_200_OK와_생성된_브랜드_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(brandService.registerBrand(any())).willReturn(new BrandResult(1L, "나이키", now, now));

        BrandCreateRequest request = new BrandCreateRequest("나이키");

        // when & then
        mockMvc.perform(post("/api-admin/v1/brands")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("나이키"));
    }

    @Test
    void 관리자_헤더가_없으면_브랜드_등록시_403을_반환한다() throws Exception {
        // given
        BrandCreateRequest request = new BrandCreateRequest("나이키");

        // when & then
        mockMvc.perform(post("/api-admin/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더_값이_잘못되면_브랜드_등록시_403을_반환한다() throws Exception {
        // given
        BrandCreateRequest request = new BrandCreateRequest("나이키");

        // when & then
        mockMvc.perform(post("/api-admin/v1/brands")
                        .header(LDAP_HEADER, "invalid.ldap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 브랜드_목록_조회_시_200_OK와_페이징된_브랜드_목록을_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        BrandResult brandResult = new BrandResult(1L, "나이키", now, now);
        given(brandService.getBrands(any())).willReturn(new PageImpl<>(List.of(brandResult), PageRequest.of(0, 20), 1));

        // when & then
        mockMvc.perform(get("/api-admin/v1/brands")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("나이키"));
    }

    @Test
    void 관리자_헤더가_없으면_브랜드_목록_조회시_403을_반환한다() throws Exception {
        mockMvc.perform(get("/api-admin/v1/brands"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 브랜드_상세_조회_시_200_OK와_브랜드_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(brandService.getBrand(1L)).willReturn(new BrandResult(1L, "나이키", now, now));

        // when & then
        mockMvc.perform(get("/api-admin/v1/brands/1")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("나이키"));
    }

    @Test
    void 관리자_헤더가_없으면_브랜드_상세_조회시_403을_반환한다() throws Exception {
        mockMvc.perform(get("/api-admin/v1/brands/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 브랜드_수정_시_200_OK와_수정된_브랜드_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(brandService.modifyBrand(eq(1L), any())).willReturn(new BrandResult(1L, "아디다스", now, now));

        BrandUpdateRequest request = new BrandUpdateRequest("아디다스");

        // when & then
        mockMvc.perform(put("/api-admin/v1/brands/1")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("아디다스"));
    }

    @Test
    void 관리자_헤더가_없으면_브랜드_수정시_403을_반환한다() throws Exception {
        BrandUpdateRequest request = new BrandUpdateRequest("아디다스");

        mockMvc.perform(put("/api-admin/v1/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 브랜드_삭제_시_200_OK를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api-admin/v1/brands/1")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk());

        verify(brandFacade).deleteBrand(1L);
    }

    @Test
    void 관리자_헤더가_없으면_브랜드_삭제시_403을_반환한다() throws Exception {
        mockMvc.perform(delete("/api-admin/v1/brands/1"))
                .andExpect(status().isForbidden());
    }
}
