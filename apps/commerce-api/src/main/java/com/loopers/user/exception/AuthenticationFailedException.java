package com.loopers.user.exception;

public class AuthenticationFailedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "인증에 실패했습니다.";

    public AuthenticationFailedException() {
        super(DEFAULT_MESSAGE);
    }

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
