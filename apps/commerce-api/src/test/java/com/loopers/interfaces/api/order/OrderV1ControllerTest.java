package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderV1Controller.class)
class OrderV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderFacade orderFacade;

    @Test
    void 주문을_생성하면_200_OK와_주문_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo orderInfo = new OrderInfo(1L, 1L, 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderFacade.createOrder(any())).willReturn(orderInfo);

        Map<String, Object> request = Map.of(
                "userId", 1,
                "orderItems", List.of(Map.of("productId", 1, "quantity", 2))
        );

        // when & then
        mockMvc.perform(post("/api/v1/orders")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
