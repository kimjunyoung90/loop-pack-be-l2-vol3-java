package com.loopers.application.coupon;

import com.loopers.application.coupon.command.CouponUpdateCommand;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponIssueRequestRepository;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private CouponIssueRequestRepository couponIssueRequestRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CouponService couponService;

    @Test
    void 쿠폰_조회_시_쿠폰이_존재하지_않으면_NOT_FOUND_예외가_발생한다() {
        // given
        given(couponRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.getCoupon(1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하지_않는_쿠폰_수정_시_NOT_FOUND_예외가_발생한다() {
        // given
        given(couponRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());
        CouponUpdateCommand command = new CouponUpdateCommand("쿠폰", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7), 100);

        // when & then
        assertThatThrownBy(() -> couponService.modifyCoupon(1L, command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하지_않는_쿠폰_삭제_시_NOT_FOUND_예외가_발생한다() {
        // given
        given(couponRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.deleteCoupon(1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 존재하지_않는_발급요청으로_쿠폰_발급_시_무시된다() {
        // given
        given(couponIssueRequestRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        // when
        couponService.issueCoupon(1L, 1L, 1L);

        // then - 예외 없이 정상 종료 (요청을 찾을 수 없으면 무시)
    }

    @Test
    void 존재하지_않는_사용자_쿠폰_사용_시_예외가_발생한다() {
        // given
        given(userCouponRepository.findByIdWithLockAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(1L, 1L, 50000))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_사용자_쿠폰_복원_시_예외가_발생한다() {
        // given
        given(userCouponRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.restoreCoupon(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }
}
