package com.loopers.interfaces.api.order.admin;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderService;
import com.loopers.application.user.UserService;
import com.loopers.interfaces.api.auth.AdminAuthInterceptor;
import com.loopers.interfaces.api.auth.LoginUserArgumentResolver;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderAdminV1Controller.class)
@Import({AdminAuthInterceptor.class, LoginUserArgumentResolver.class})
class OrderAdminV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    private static final String LDAP_HEADER = "X-Loopers-Ldap";
    private static final String VALID_LDAP = "loopers.admin";

    @Test
    void 관리자_헤더가_유효하면_주문_상세_조회에_성공한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo orderInfo = new OrderInfo(1L, 1L, "COMPLETED", 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.getOrder(1L)).willReturn(orderInfo);

        // when & then
        mockMvc.perform(get("/api-admin/v1/orders/1")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalPrice").value(100000))
                .andExpect(jsonPath("$.data.orderItems[0].productName").value("운동화"));
    }

    @Test
    void 관리자_헤더가_없으면_주문_상세_조회시_403을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api-admin/v1/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_헤더_값이_잘못되면_주문_상세_조회시_403을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api-admin/v1/orders/1")
                        .header(LDAP_HEADER, "invalid.ldap"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 존재하지_않는_주문_상세_조회시_404_NOT_FOUND를_반환한다() throws Exception {
        // given
        given(orderService.getOrder(999L)).willThrow(new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api-admin/v1/orders/999")
                        .header(LDAP_HEADER, VALID_LDAP))
                .andExpect(status().isNotFound());
    }
}
