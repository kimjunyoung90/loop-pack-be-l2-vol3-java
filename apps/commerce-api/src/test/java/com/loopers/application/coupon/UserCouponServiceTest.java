package com.loopers.application.coupon;

import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private UserCouponService userCouponService;

    @Test
    void 이미_발급받은_쿠폰을_다시_발급_요청하면_CONFLICT_예외가_발생한다() {
        // given
        LocalDate expiredAt = LocalDate.now().plusDays(7);
        CouponInfo couponInfo = new CouponInfo(1L, "테스트 쿠폰", DiscountType.FIXED, 1000, null, expiredAt, null, null);
        given(userCouponRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userCouponService.createUserCoupon(1L, couponInfo))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.CONFLICT));
    }

}
