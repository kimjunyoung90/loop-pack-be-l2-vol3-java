package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.application.user.UserService;
import com.loopers.interfaces.api.auth.AdminAuthInterceptor;
import com.loopers.interfaces.api.auth.LoginUserArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductV1Controller.class)
@Import({LoginUserArgumentResolver.class, AdminAuthInterceptor.class})
class ProductV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserService userService;

    private static final String LOGIN_ID_HEADER = "X-Loopers-LoginId";
    private static final String LOGIN_PW_HEADER = "X-Loopers-LoginPw";

    @Test
    void 상품_목록을_조회하면_200_OK와_페이징된_상품_목록을_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        ProductInfo productInfo = new ProductInfo(1L, 1L, "운동화", 100000, 50, now, now);
        given(productService.getProducts(PageRequest.of(0, 20)))
                .willReturn(new PageImpl<>(List.of(productInfo), PageRequest.of(0, 20), 1));

        // when & then
        mockMvc.perform(get("/api/v1/products")
                        .header(LOGIN_ID_HEADER, "testuser")
                        .header(LOGIN_PW_HEADER, "password1!")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("운동화"))
                .andExpect(jsonPath("$.data.content[0].price").value(100000));
    }

    @Test
    void 상품_상세를_조회하면_200_OK와_상품_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(productService.getProduct(1L))
                .willReturn(new ProductInfo(1L, 1L, "운동화", 100000, 50, now, now));

        // when & then
        mockMvc.perform(get("/api/v1/products/1")
                        .header(LOGIN_ID_HEADER, "testuser")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("운동화"))
                .andExpect(jsonPath("$.data.price").value(100000));
    }
}
