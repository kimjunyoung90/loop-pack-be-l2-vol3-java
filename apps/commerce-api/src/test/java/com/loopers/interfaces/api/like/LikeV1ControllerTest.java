package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.LikeInfo;
import com.loopers.application.like.LikeService;
import com.loopers.application.user.UserService;
import com.loopers.domain.user.User;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeV1Controller.class)
@Import({LoginUserArgumentResolver.class, AdminAuthInterceptor.class})
class LikeV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeFacade likeFacade;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private UserService userService;

    private static final String LOGIN_ID_HEADER = "X-Loopers-LoginId";
    private static final String LOGIN_PW_HEADER = "X-Loopers-LoginPw";

    @Test
    void 좋아요를_등록하면_200_OK와_좋아요_정보를_반환한다() throws Exception {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        LikeInfo likeInfo = new LikeInfo(1L, 1L, 1L, now);

        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("loginId");
        given(userService.authenticateUser("loginId", "password1!")).willReturn(mockUser);
        given(likeFacade.createLike(eq(1L), eq(1L))).willReturn(likeInfo);

        // when & then
        mockMvc.perform(post("/api/v1/products/1/likes")
                        .header(LOGIN_ID_HEADER, "loginId")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.productId").value(1));
    }

    @Test
    void 존재하지_않는_상품에_좋아요를_등록하면_404_NOT_FOUND를_반환한다() throws Exception {
        // given
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("loginId");
        given(userService.authenticateUser("loginId", "password1!")).willReturn(mockUser);
        given(likeFacade.createLike(eq(1L), eq(999L)))
                .willThrow(new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(post("/api/v1/products/999/likes")
                        .header(LOGIN_ID_HEADER, "loginId")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 이미_좋아요한_상품에_다시_좋아요하면_409_CONFLICT를_반환한다() throws Exception {
        // given
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("loginId");
        given(userService.authenticateUser("loginId", "password1!")).willReturn(mockUser);
        given(likeFacade.createLike(eq(1L), eq(1L)))
                .willThrow(new CoreException(ErrorType.CONFLICT, "이미 좋아요한 상품입니다."));

        // when & then
        mockMvc.perform(post("/api/v1/products/1/likes")
                        .header(LOGIN_ID_HEADER, "loginId")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isConflict());
    }

    @Test
    void 인증_헤더가_없으면_좋아요_등록시_401_UNAUTHORIZED를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/products/1/likes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 좋아요를_취소하면_200_OK를_반환한다() throws Exception {
        // given
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("loginId");
        given(userService.authenticateUser("loginId", "password1!")).willReturn(mockUser);
        willDoNothing().given(likeService).deleteLike(eq(1L), eq(1L));

        // when & then
        mockMvc.perform(delete("/api/v1/products/1/likes")
                        .header(LOGIN_ID_HEADER, "loginId")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isOk());
    }

    @Test
    void 좋아요하지_않은_상품의_좋아요를_취소하면_404_NOT_FOUND를_반환한다() throws Exception {
        // given
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);
        given(mockUser.getLoginId()).willReturn("loginId");
        given(userService.authenticateUser("loginId", "password1!")).willReturn(mockUser);
        willThrow(new CoreException(ErrorType.NOT_FOUND, "좋아요를 찾을 수 없습니다."))
                .given(likeService).deleteLike(eq(1L), eq(999L));

        // when & then
        mockMvc.perform(delete("/api/v1/products/999/likes")
                        .header(LOGIN_ID_HEADER, "loginId")
                        .header(LOGIN_PW_HEADER, "password1!"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 인증_헤더가_없으면_좋아요_취소시_401_UNAUTHORIZED를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/products/1/likes"))
                .andExpect(status().isUnauthorized());
    }
}
