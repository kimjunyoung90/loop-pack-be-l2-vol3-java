package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderService;
import com.loopers.application.user.UserService;
import com.loopers.domain.user.User;
import com.loopers.interfaces.api.auth.AdminAuthInterceptor;
import com.loopers.interfaces.api.auth.LoginUserArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderV1Controller.class)
@Import({LoginUserArgumentResolver.class, AdminAuthInterceptor.class})
class OrderV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderFacade orderFacade;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    private static final String LOGIN_ID_HEADER = "X-Loopers-LoginId";
    private static final String LOGIN_PW_HEADER = "X-Loopers-LoginPw";

    @Test
    void 주문을_생성하면_200_OK와_주문_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo orderInfo = new OrderInfo(1L, 1L, "COMPLETED", 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderFacade.createOrder(any())).willReturn(orderInfo);

        Map<String, Object> request = Map.of(
                "userId", 1,
                "orderItems", List.of(Map.of("productId", 1, "quantity", 2))
        );

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .header(LOGIN_ID_HEADER, "testuser")
                        .header(LOGIN_PW_HEADER, "password1!")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalPrice").value(100000))
                .andExpect(jsonPath("$.data.orderItems[0].productName").value("운동화"));
    }

    @Test
    void userId가_null이면_400_BAD_REQUEST를_반환한다() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "orderItems", List.of(Map.of("productId", 1, "quantity", 2))
        );

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .header(LOGIN_ID_HEADER, "testuser")
                        .header(LOGIN_PW_HEADER, "password1!")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void orderItems가_빈_목록이면_400_BAD_REQUEST를_반환한다() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "userId", 1,
                "orderItems", List.of()
        );

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .header(LOGIN_ID_HEADER, "testuser")
                        .header(LOGIN_PW_HEADER, "password1!")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 주문을_취소하면_200_OK와_취소된_주문_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo orderInfo = new OrderInfo(1L, 1L, "CANCELLED", 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);

        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("loginId");
        given(userService.authenticateUser("loginId", "password1!")).willReturn(mockUser);
        given(orderFacade.cancelOrder(1L, 1L)).willReturn(orderInfo);

        // when & then
        mockMvc.perform(patch("/api/v1/orders/1/cancel")
                        .header("X-Loopers-LoginId", "loginId")
                        .header("X-Loopers-LoginPw", "password1!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.totalPrice").value(100000));
    }

    @Test
    void 인증_헤더가_없으면_주문_취소시_401_UNAUTHORIZED를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/orders/1/cancel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 주문_목록을_조회하면_200_OK와_주문_목록을_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo orderInfo = new OrderInfo(1L, 1L, "COMPLETED", 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);

        Page<OrderInfo> orderPage = new PageImpl<>(List.of(orderInfo), PageRequest.of(0, 20), 1);

        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("loginId");
        given(userService.authenticateUser("loginId", "password1!")).willReturn(mockUser);
        given(orderService.getOrders(eq(1L), any(LocalDate.class), any(LocalDate.class), any())).willReturn(orderPage);

        // when & then
        mockMvc.perform(get("/api/v1/orders")
                        .header(LOGIN_ID_HEADER, "loginId")
                        .header(LOGIN_PW_HEADER, "password1!")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value(1))
                .andExpect(jsonPath("$.data.content[0].totalPrice").value(100000))
                .andExpect(jsonPath("$.data.content[0].orderItems[0].productName").value("운동화"));
    }

    @Test
    void 인증_헤더가_없으면_주문_목록_조회시_401_UNAUTHORIZED를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/orders")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isUnauthorized());
    }
}
