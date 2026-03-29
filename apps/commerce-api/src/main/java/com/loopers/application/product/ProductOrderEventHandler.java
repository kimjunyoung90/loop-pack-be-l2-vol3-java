package com.loopers.application.product;

import com.loopers.domain.order.event.OrderCancelledEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Comparator;

@RequiredArgsConstructor
@Component
public class ProductOrderEventHandler {

	private final ProductService productService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleOrderPlaced(OrderPlacedEvent event) {
		event.stockItems().stream()
				.sorted(Comparator.comparing(OrderPlacedEvent.ItemStock::productId))
				.forEach(itemStock -> productService.deductStock(itemStock.productId(), itemStock.quantity()));
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleOrderCancelled(OrderCancelledEvent event) {
		event.stockItems().stream()
				.sorted(Comparator.comparing(OrderCancelledEvent.ItemStock::productId))
				.forEach(item -> productService.restoreStock(item.productId(), item.quantity()));
	}
}
