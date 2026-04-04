package com.loopers.interfaces.api.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.result.CouponIssueRequestResult;
import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.application.user.UserService;
import com.loopers.domain.coupon.CouponRequestStatus;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.user.User;
import com.loopers.interfaces.api.auth.AdminAuthInterceptor;
import com.loopers.interfaces.api.auth.LoginUserArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponV1Controller.class)
@Import({AdminAuthInterceptor.class, LoginUserArgumentResolver.class})
class CouponV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    @MockitoBean
    private UserService userService;

    private static final String LOGIN_ID_HEADER = "X-Loopers-LoginId";
    private static final String LOGIN_PW_HEADER = "X-Loopers-LoginPw";

    private void setupAuthUser() {
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("testuser");
        given(userService.authenticateUser("testuser", "password1!")).willReturn(mockUser);
    }

    @Test
    void 쿠폰_발급_요청_시_PENDING_상태의_발급요청을_반환한다() throws Exception {
        // given
        setupAuthUser();
        ZonedDateTime now = ZonedDateTime.now();
        CouponIssueRequestResult result = new CouponIssueRequestResult(
                1L, 1L, 1L, CouponRequestStatus.PENDING, null, now, now
        );
        given(couponService.requestIssueCoupon(eq(1L), eq(1L))).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/v1/coupons/1/issues")
                        .header(LOGIN_ID_HEADER, "testuser")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void 내_쿠폰_목록_조회_시_쿠폰_목록을_반환한다() throws Exception {
        // given
        setupAuthUser();
        LocalDate expiredAt = LocalDate.now().plusDays(30);
        UserCouponResult result = new UserCouponResult(
                1L, 1L, 1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null,
                "AVAILABLE", expiredAt, null, null
        );
        given(couponService.getUserCoupons(eq(1L))).willReturn(List.of(result));

        // when & then
        mockMvc.perform(get("/api/v1/coupons/me")
                        .header(LOGIN_ID_HEADER, "testuser")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].couponName").value("테스트 쿠폰"))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));
    }

}
