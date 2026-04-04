package com.loopers.interfaces.api.coupon.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.application.user.UserService;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.interfaces.api.coupon.admin.request.CouponCreateRequest;
import com.loopers.interfaces.api.coupon.admin.request.CouponUpdateRequest;
import com.loopers.interfaces.api.auth.AdminAuthInterceptor;
import com.loopers.interfaces.api.auth.LoginUserArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponAdminV1Controller.class)
@Import({AdminAuthInterceptor.class, LoginUserArgumentResolver.class})
class CouponAdminV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    @MockitoBean
    private UserService userService;

    private static final String LDAP_HEADER = "X-Loopers-Ldap";
    private static final String VALID_LDAP = "test";

    @Test
    void 쿠폰_생성_시_쿠폰정보를_반환한다() throws Exception {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(30);
        CouponResult couponResult = new CouponResult(1L, "신규 쿠폰", DiscountType.FIXED, 3000, 10000, expiredAt, 100, 0, null, null);
        given(couponService.registerCoupon(any())).willReturn(couponResult);

        CouponCreateRequest request = new CouponCreateRequest(
                "신규 쿠폰", DiscountType.FIXED, 3000, 10000, expiredAt, 100
        );

        // when & then
        mockMvc.perform(post("/api-admin/v1/coupons")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("신규 쿠폰"))
                .andExpect(jsonPath("$.data.discountType").value("FIXED"))
                .andExpect(jsonPath("$.data.discountValue").value(3000))
                .andExpect(jsonPath("$.data.minOrderAmount").value(10000));
    }

    @Test
    void 쿠폰_생성_시_쿠폰명이_비어있으면_400을_반환한다() throws Exception {
        // given
        CouponCreateRequest request = new CouponCreateRequest(
                "", DiscountType.FIXED, 3000, null, LocalDate.now().plusDays(7), 100
        );

        // when & then
        mockMvc.perform(post("/api-admin/v1/coupons")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_생성_시_할인유형이_null이면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {"name": "쿠폰", "discountType": null, "discountValue": 3000, "expiredAt": "%s"}
                """.formatted(LocalDate.now().plusDays(7));

        // when & then
        mockMvc.perform(post("/api-admin/v1/coupons")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_생성_시_할인값이_null이면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {"name": "쿠폰", "discountType": "FIXED", "discountValue": null, "expiredAt": "%s"}
                """.formatted(LocalDate.now().plusDays(7));

        // when & then
        mockMvc.perform(post("/api-admin/v1/coupons")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_생성_시_만료일이_null이면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {"name": "쿠폰", "discountType": "FIXED", "discountValue": 3000, "expiredAt": null}
                """;

        // when & then
        mockMvc.perform(post("/api-admin/v1/coupons")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_목록_조회_시_페이징된_쿠폰정보를_반환한다() throws Exception {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(30);
        CouponResult couponResult = new CouponResult(1L, "쿠폰", DiscountType.FIXED, 1000, null, expiredAt, 100, 0, null, null);
        given(couponService.getCoupons(any())).willReturn(new PageImpl<>(List.of(couponResult)));

        // when & then
        mockMvc.perform(get("/api-admin/v1/coupons")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("쿠폰"));
    }

    @Test
    void 쿠폰_상세_조회_시_쿠폰정보를_반환한다() throws Exception {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(30);
        CouponResult couponResult = new CouponResult(1L, "쿠폰", DiscountType.RATE, 10, 5000, expiredAt, 100, 0, null, null);
        given(couponService.getCoupon(1L)).willReturn(couponResult);

        // when & then
        mockMvc.perform(get("/api-admin/v1/coupons/1")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.discountType").value("RATE"))
                .andExpect(jsonPath("$.data.discountValue").value(10))
                .andExpect(jsonPath("$.data.minOrderAmount").value(5000));
    }

    @Test
    void 쿠폰_수정_시_수정된_쿠폰정보를_반환한다() throws Exception {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(30);
        CouponResult couponResult = new CouponResult(1L, "수정 쿠폰", DiscountType.RATE, 15, 5000, expiredAt, 100, 0, null, null);
        given(couponService.modifyCoupon(any(), any())).willReturn(couponResult);

        CouponUpdateRequest request = new CouponUpdateRequest(
                "수정 쿠폰", DiscountType.RATE, 15, 5000, expiredAt, 100
        );

        // when & then
        mockMvc.perform(put("/api-admin/v1/coupons/1")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 쿠폰"))
                .andExpect(jsonPath("$.data.discountType").value("RATE"));
    }

    @Test
    void 쿠폰_삭제_시_200_OK를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api-admin/v1/coupons/1")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk());
    }

    @Test
    void 쿠폰_수정_시_쿠폰명이_비어있으면_400을_반환한다() throws Exception {
        // given
        CouponUpdateRequest request = new CouponUpdateRequest(
                "", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7), 100
        );

        // when & then
        mockMvc.perform(put("/api-admin/v1/coupons/1")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_수정_시_할인유형이_null이면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {"name": "쿠폰", "discountType": null, "discountValue": 1000, "expiredAt": "%s"}
                """.formatted(LocalDate.now().plusDays(7));

        // when & then
        mockMvc.perform(put("/api-admin/v1/coupons/1")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_수정_시_할인값이_null이면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {"name": "쿠폰", "discountType": "FIXED", "discountValue": null, "expiredAt": "%s"}
                """.formatted(LocalDate.now().plusDays(7));

        // when & then
        mockMvc.perform(put("/api-admin/v1/coupons/1")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_수정_시_만료일이_null이면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {"name": "쿠폰", "discountType": "FIXED", "discountValue": 1000, "expiredAt": null}
                """;

        // when & then
        mockMvc.perform(put("/api-admin/v1/coupons/1")
                        .header(LDAP_HEADER, VALID_LDAP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰_발급_내역_조회_시_페이징된_발급_내역을_반환한다() throws Exception {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(30);
        UserCouponResult result = new UserCouponResult(
                1L, 1L, 1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null,
                "AVAILABLE", expiredAt, null, null
        );
        given(couponService.getIssuedCoupons(any(), any())).willReturn(new PageImpl<>(List.of(result)));

        // when & then
        mockMvc.perform(get("/api-admin/v1/coupons/1/issues")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value(1))
                .andExpect(jsonPath("$.data.content[0].couponName").value("테스트 쿠폰"))
                .andExpect(jsonPath("$.data.content[0].status").value("AVAILABLE"));
    }
}
