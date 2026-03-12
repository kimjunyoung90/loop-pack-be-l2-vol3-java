package com.loopers.infrastructure.product;

import com.loopers.domain.like.QProductLikeCount;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductWithLikeCount;
import com.loopers.domain.product.QProduct;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {
	private final ProductJpaRepository productJpaRepository;
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Product> findAllByDeletedAtIsNull(Pageable pageable) {
		return productJpaRepository.findAllByDeletedAtIsNull(pageable);
	}

	@Override
	public List<Product> findAllByBrandId(Long brandId) {
		return productJpaRepository.findAllByBrandId(brandId);
	}

	@Override
	public List<Product> findAllByBrandIdAndDeletedAtIsNull(Long brandId) {
		return productJpaRepository.findAllByBrandIdAndDeletedAtIsNull(brandId);
	}

	@Override
	public Optional<Product> findById(Long productId) {
		return productJpaRepository.findById(productId);
	}

	@Override
	public Optional<Product> findByIdAndDeletedAtIsNull(Long productId) {
		return productJpaRepository.findByIdAndDeletedAtIsNull(productId);
	}

	@Override
	public Optional<Product> findByIdWithLockAndDeletedAtIsNull(Long productId) {
		return productJpaRepository.findByIdWithLockAndDeletedAtIsNull(productId);
	}

	@Override
	public Product save(Product product) {
		return productJpaRepository.save(product);
	}

	@Override
	public Optional<ProductWithLikeCount> findWithLikeCountByIdAndDeletedAtIsNull(Long productId) {
		QProduct p = QProduct.product;
		QProductLikeCount plc = QProductLikeCount.productLikeCount;

		ProductWithLikeCount result = queryFactory
				.select(Projections.constructor(ProductWithLikeCount.class,
						p.id, p.brandId, p.name, p.price, p.stock,
						plc.likeCount.coalesce(0), p.createdAt, p.updatedAt
				))
				.from(p)
				.leftJoin(plc).on(p.id.eq(plc.productId))
				.where(
						p.id.eq(productId),
						p.deletedAt.isNull()
				)
				.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public Page<ProductWithLikeCount> findAllWithLikeCountByDeletedAtIsNull(Pageable pageable) {
		QProduct p = QProduct.product;
		QProductLikeCount plc = QProductLikeCount.productLikeCount;

		List<ProductWithLikeCount> content = queryFactory
				.select(Projections.constructor(ProductWithLikeCount.class,
						p.id, p.brandId, p.name, p.price, p.stock,
						plc.likeCount.coalesce(0), p.createdAt, p.updatedAt
				))
				.from(p)
				.leftJoin(plc).on(p.id.eq(plc.productId))
				.where(p.deletedAt.isNull())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.orderBy(getOrderSpecifiers(pageable, p, plc))
				.fetch();

		Long total = queryFactory
				.select(p.count())
				.from(p)
				.where(p.deletedAt.isNull())
				.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	@Override
	public Page<ProductWithLikeCount> findAllWithLikeCountByBrandIdAndDeletedAtIsNull(Long brandId, Pageable pageable) {
		QProduct p = QProduct.product;
		QProductLikeCount plc = QProductLikeCount.productLikeCount;

		List<ProductWithLikeCount> content = queryFactory
				.select(Projections.constructor(ProductWithLikeCount.class,
						p.id, p.brandId, p.name, p.price, p.stock,
						plc.likeCount.coalesce(0), p.createdAt, p.updatedAt
				))
				.from(p)
				.leftJoin(plc).on(p.id.eq(plc.productId))
				.where(
						p.brandId.eq(brandId),
						p.deletedAt.isNull()
				)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.orderBy(getOrderSpecifiers(pageable, p, plc))
				.fetch();

		Long total = queryFactory
				.select(p.count())
				.from(p)
				.where(
						p.brandId.eq(brandId),
						p.deletedAt.isNull()
				)
				.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable, QProduct p, QProductLikeCount plc) {
		return pageable.getSort().stream()
				.map(order -> {
					Order direction = order.isAscending() ? Order.ASC : Order.DESC;
					return switch (order.getProperty()) {
						case "like" -> new OrderSpecifier<>(direction, plc.likeCount.coalesce(0));
						case "price" -> new OrderSpecifier<>(direction, p.price);
						case "name" -> new OrderSpecifier<>(direction, p.name);
						case "createdAt" -> new OrderSpecifier<>(direction, p.createdAt);
						default -> new OrderSpecifier<>(direction, p.createdAt);
					};
				})
				.toArray(OrderSpecifier[]::new);
	}

}
