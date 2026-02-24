package com.loopers.interfaces.api.brand.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.application.brand.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
class BrandAdminV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private BrandFacade brandFacade;

    private static final String LDAP_HEADER = "X-Loopers-Ldap";
    private static final String VALID_LDAP = "loopers.admin";

    @Test
    void 관리자_헤더가_유효하면_브랜드_등록에_성공한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(brandService.createBrand(any())).willReturn(new BrandInfo(1L, "나이키", now, now));

        BrandAdminV1Dto.CreateBrandRequest request = new BrandAdminV1Dto.CreateBrandRequest("나이키");

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
        BrandAdminV1Dto.CreateBrandRequest request = new BrandAdminV1Dto.CreateBrandRequest("나이키");

        // when & then
        mockMvc.perform(post("/api-admin/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더_값이_잘못되면_브랜드_등록시_403을_반환한다() throws Exception {
        // given
        BrandAdminV1Dto.CreateBrandRequest request = new BrandAdminV1Dto.CreateBrandRequest("나이키");

        // when & then
        mockMvc.perform(post("/api-admin/v1/brands")
                        .header(LDAP_HEADER, "invalid.ldap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더가_유효하면_브랜드_목록_조회에_성공한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        BrandInfo brandInfo = new BrandInfo(1L, "나이키", now, now);
        given(brandService.getBrands(any())).willReturn(new PageImpl<>(List.of(brandInfo), PageRequest.of(0, 20), 1));

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
    void 관리자_헤더가_유효하면_브랜드_상세_조회에_성공한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(brandService.getBrand(1L)).willReturn(new BrandInfo(1L, "나이키", now, now));

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
    void 관리자_헤더가_유효하면_브랜드_수정에_성공한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(brandService.updateBrand(eq(1L), any())).willReturn(new BrandInfo(1L, "아디다스", now, now));

        BrandAdminV1Dto.UpdateBrandRequest request = new BrandAdminV1Dto.UpdateBrandRequest("아디다스");

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
        BrandAdminV1Dto.UpdateBrandRequest request = new BrandAdminV1Dto.UpdateBrandRequest("아디다스");

        mockMvc.perform(put("/api-admin/v1/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더가_유효하면_브랜드_삭제에_성공한다() throws Exception {
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
