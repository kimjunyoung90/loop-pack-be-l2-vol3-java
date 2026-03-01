package com.loopers.application.order;

import com.loopers.application.product.ProductService;
import com.loopers.application.user.UserService;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderFacade orderFacade;

    @Test
    void 유효한_사용자와_상품으로_주문하면_재고_차감_후_OrderInfo를_반환한다() {
        // given
        Brand brand = Brand.builder().name("나이키").build();
        Product product = Product.builder()
                .brand(brand)
                .name("운동화")
                .price(50000)
                .stock(10)
                .build();

        CreateOrderCommand command = new CreateOrderCommand(1L, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(1L, 2)
        ));

        given(userService.findUser(1L)).willReturn(null);
        given(productService.findProduct(1L)).willReturn(product);

        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo expectedInfo = new OrderInfo(1L, 1L, "COMPLETED", 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.createOrder(eq(1L), any())).willReturn(expectedInfo);

        // when
        OrderInfo result = orderFacade.createOrder(command);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(100000);
        assertThat(product.getStock()).isEqualTo(8);
    }

    @Test
    void 존재하지_않는_사용자로_주문하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(999L, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(1L, 2)
        ));
        willThrow(new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .given(userService).findUser(999L);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하지_않는_상품으로_주문하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(1L, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(999L, 2)
        ));
        given(userService.findUser(1L)).willReturn(null);
        willThrow(new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."))
                .given(productService).findProduct(999L);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 재고가_부족한_상품이_포함되면_CoreException이_발생한다() {
        // given
        Brand brand = Brand.builder().name("나이키").build();
        Product product = Product.builder()
                .brand(brand)
                .name("운동화")
                .price(50000)
                .stock(1)
                .build();

        CreateOrderCommand command = new CreateOrderCommand(1L, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(1L, 5)
        ));
        given(userService.findUser(1L)).willReturn(null);
        given(productService.findProduct(1L)).willReturn(product);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 주문을_취소하면_상태가_CANCELLED인_OrderInfo와_복원된_재고를_반환한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(2)
                .build();
        order.addOrderItem(item);
        given(orderService.findOrder(1L)).willReturn(order);

        Brand brand = Brand.builder().name("나이키").build();
        Product product = Product.builder()
                .brand(brand)
                .name("운동화")
                .price(50000)
                .stock(8)
                .build();
        given(productService.findProduct(1L)).willReturn(product);

        // when
        OrderInfo result = orderFacade.cancelOrder(1L, 1L);

        // then
        assertThat(result.status()).isEqualTo("CANCELLED");
        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    void 본인_주문이_아니면_CoreException_FORBIDDEN이_발생한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        given(orderService.findOrder(1L)).willReturn(order);

        // when & then
        assertThatThrownBy(() -> orderFacade.cancelOrder(999L, 1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 이미_취소된_주문이면_CoreException_BAD_REQUEST가_발생한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        order.cancel();
        given(orderService.findOrder(1L)).willReturn(order);

        // when & then
        assertThatThrownBy(() -> orderFacade.cancelOrder(1L, 1L))
                .isInstanceOf(CoreException.class);
    }
}
