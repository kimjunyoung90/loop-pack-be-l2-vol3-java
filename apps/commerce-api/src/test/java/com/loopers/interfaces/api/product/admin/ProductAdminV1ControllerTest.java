package com.loopers.interfaces.api.product.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
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

@WebMvcTest(ProductAdminV1Controller.class)
class ProductAdminV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductFacade productFacade;

    private static final String LDAP_HEADER = "X-Loopers-Ldap";
    private static final String VALID_LDAP = "loopers.admin";

    @Test
    void 관리자_헤더가_유효하면_상품을_등록하고_200_OK와_생성된_상품_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(productFacade.createProduct(any())).willReturn(
                new ProductInfo(1L, 1L, "운동화", 100000, 50, now, now)
        );

        ProductAdminV1Dto.CreateProductRequest request =
                new ProductAdminV1Dto.CreateProductRequest(1L, "운동화", 100000, 50);

        // when & then
        mockMvc.perform(post("/api-admin/v1/products")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("운동화"))
                .andExpect(jsonPath("$.data.price").value(100000));
    }

    @Test
    void 관리자_헤더가_없으면_상품_등록시_403을_반환한다() throws Exception {
        // given
        ProductAdminV1Dto.CreateProductRequest request =
                new ProductAdminV1Dto.CreateProductRequest(1L, "운동화", 100000, 50);

        // when & then
        mockMvc.perform(post("/api-admin/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더_값이_잘못되면_상품_등록시_403을_반환한다() throws Exception {
        // given
        ProductAdminV1Dto.CreateProductRequest request =
                new ProductAdminV1Dto.CreateProductRequest(1L, "운동화", 100000, 50);

        // when & then
        mockMvc.perform(post("/api-admin/v1/products")
                        .header(LDAP_HEADER, "invalid.ldap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더가_유효하면_상품_목록_조회시_200_OK와_페이징된_상품_목록을_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        ProductInfo productInfo = new ProductInfo(1L, 1L, "운동화", 100000, 50, now, now);
        given(productService.getProducts(any())).willReturn(
                new PageImpl<>(List.of(productInfo), PageRequest.of(0, 20), 1)
        );

        // when & then
        mockMvc.perform(get("/api-admin/v1/products")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("운동화"));
    }

    @Test
    void 관리자_헤더가_없으면_상품_목록_조회시_403을_반환한다() throws Exception {
        mockMvc.perform(get("/api-admin/v1/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더가_유효하면_상품_상세_조회시_200_OK와_상품_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(productService.getProduct(1L)).willReturn(
                new ProductInfo(1L, 1L, "운동화", 100000, 50, now, now)
        );

        // when & then
        mockMvc.perform(get("/api-admin/v1/products/1")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("운동화"))
                .andExpect(jsonPath("$.data.price").value(100000));
    }

    @Test
    void 관리자_헤더가_없으면_상품_상세_조회시_403을_반환한다() throws Exception {
        mockMvc.perform(get("/api-admin/v1/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더가_유효하면_상품을_수정하고_200_OK와_수정된_상품_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(productFacade.updateProduct(eq(1L), any())).willReturn(
                new ProductInfo(1L, 2L, "슬리퍼", 50000, 30, now, now)
        );

        ProductAdminV1Dto.UpdateProductRequest request =
                new ProductAdminV1Dto.UpdateProductRequest(2L, "슬리퍼", 50000, 30);

        // when & then
        mockMvc.perform(put("/api-admin/v1/products/1")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("슬리퍼"))
                .andExpect(jsonPath("$.data.price").value(50000));
    }

    @Test
    void 관리자_헤더가_없으면_상품_수정시_403을_반환한다() throws Exception {
        // given
        ProductAdminV1Dto.UpdateProductRequest request =
                new ProductAdminV1Dto.UpdateProductRequest(2L, "슬리퍼", 50000, 30);

        // when & then
        mockMvc.perform(put("/api-admin/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더가_유효하면_상품을_삭제하고_200_OK를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api-admin/v1/products/1")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void 관리자_헤더가_없으면_상품_삭제시_403을_반환한다() throws Exception {
        mockMvc.perform(delete("/api-admin/v1/products/1"))
                .andExpect(status().isForbidden());
    }
}
