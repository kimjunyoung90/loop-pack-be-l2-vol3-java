package com.loopers.user;

import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.interfaces.api.user.ChangePasswordRequest;
import com.loopers.interfaces.api.user.CreateUserRequest;
import com.loopers.interfaces.api.user.CreateUserResponse;
import com.loopers.interfaces.api.user.GetMyInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import static com.loopers.interfaces.api.user.UserController.LOGIN_ID_HEADER;
import static com.loopers.interfaces.api.user.UserController.LOGIN_PW_HEADER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MySqlTestContainersConfig.class)
public class UserE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void 유효한_데이터로_회원가입_요청하면_회원가입에_성공한다() {
        // given
        CreateUserRequest request = new CreateUserRequest(
                "testuser",
                "Password1!",
                "홍길동",
                "1990-01-01",
                "test@example.com"
        );

        //실제 HTTP 요청
        ResponseEntity<CreateUserResponse> response = restTemplate.postForEntity(
                "/api/v1/users",
                request,
                CreateUserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void 존재하는_ID로_내정보_조회시_마스킹된_이름과_비밀번호_제외한_정보를_반환한다() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "myinfouser", "Pass1234!", "홍길동", "1999-01-01", "test@email.com");

        restTemplate.postForEntity("/api/v1/users", createUserRequest, Void.class);

        //when
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Loopers-LoginId", "myinfouser");
        headers.set("X-Loopers-LoginPw", "Pass1234!");
        ResponseEntity<GetMyInfoResponse> response = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET,
                new HttpEntity<>(headers), GetMyInfoResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        GetMyInfoResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.loginId()).isEqualTo("myinfouser");
        assertThat(body.name()).isEqualTo("홍길*");
        assertThat(body.birthDate()).isEqualTo("1999-01-01");
        assertThat(body.email()).isEqualTo("test@email.com");
    }

    @Test
    void 존재하지_않는_ID로_내정보_조회시_401을_반환한다() {
        //given
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Loopers-LoginId", "nonexistent99");
        headers.set("X-Loopers-LoginPw", "Pass1234!");

        //when
        ResponseEntity<GetMyInfoResponse> response = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET,
                new HttpEntity<>(headers), GetMyInfoResponse.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void 비밀번호_변경시_새_비밀번호가_기존과_동일하면_400_Bad_Request_반환() {
        // given - 사용자 생성
        String loginId = "pwsame" + (System.currentTimeMillis() % 1000);
        String currentPassword = "Password1!";

        CreateUserRequest createRequest = new CreateUserRequest(
                loginId, currentPassword, "홍길동", "1990-01-01", "test@example.com"
        );
        ResponseEntity<CreateUserResponse> createResponse = restTemplate.postForEntity("/api/v1/users", createRequest, CreateUserResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // when - 동일한 비밀번호로 변경 시도
        HttpHeaders headers = new HttpHeaders();
        headers.set(LOGIN_ID_HEADER, loginId);
        headers.set(LOGIN_PW_HEADER, currentPassword);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ChangePasswordRequest changeRequest = new ChangePasswordRequest(currentPassword);
        HttpEntity<ChangePasswordRequest> entity = new HttpEntity<>(changeRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/password",
                HttpMethod.PATCH,
                entity,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 유효한_새로운_비밀번호로_비밀번호_수정을_요청하면_200을_반환한다() {

        String id = "chgpwuser";
        String password = "Pass1234!";
        CreateUserRequest createUserRequest = new CreateUserRequest(
                id, password, "홍길동", "1999-01-01", "test@email.com");

        restTemplate.postForEntity("/api/v1/users", createUserRequest, Void.class);

        ChangePasswordRequest changeRequest = new ChangePasswordRequest("NewPass123!");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Loopers-LoginId", id);
        headers.set("X-Loopers-LoginPw", password);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/users/password", HttpMethod.PATCH,
                new HttpEntity<>(changeRequest, headers), Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 새 비밀번호로 내 정보 조회 성공 확인
        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.set("X-Loopers-LoginId", id);
        newHeaders.set("X-Loopers-LoginPw", "NewPass123!");

        ResponseEntity<GetMyInfoResponse> myInfoResponse = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET,
                new HttpEntity<>(newHeaders), GetMyInfoResponse.class
        );

        assertThat(myInfoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void 잘못된_비밀번호로_내정보_조회시_401을_반환한다() {
        // given
        String loginId = "authtest01";
        String password = "Pass1234!";
        CreateUserRequest createRequest = new CreateUserRequest(
                loginId, password, "홍길동", "1990-01-01", "test@example.com"
        );
        restTemplate.postForEntity("/api/v1/users", createRequest, Void.class);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.set(LOGIN_ID_HEADER, loginId);
        headers.set(LOGIN_PW_HEADER, "WrongPass1!");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/me", HttpMethod.GET,
                new HttpEntity<>(headers), String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void 잘못된_비밀번호로_비밀번호_변경시_401을_반환한다() {
        // given
        String loginId = "authtest02";
        String password = "Pass1234!";
        CreateUserRequest createRequest = new CreateUserRequest(
                loginId, password, "홍길동", "1990-01-01", "test@example.com"
        );
        restTemplate.postForEntity("/api/v1/users", createRequest, Void.class);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.set(LOGIN_ID_HEADER, loginId);
        headers.set(LOGIN_PW_HEADER, "WrongPass1!");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ChangePasswordRequest changeRequest = new ChangePasswordRequest("NewPass456!");
        HttpEntity<ChangePasswordRequest> entity = new HttpEntity<>(changeRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/password",
                HttpMethod.PATCH,
                entity,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}

