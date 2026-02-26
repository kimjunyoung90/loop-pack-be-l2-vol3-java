package com.loopers.support.auth;

public final class AuthConstants {

    private AuthConstants() {
    }

    public static final String LOGIN_ID_HEADER = "X-Loopers-LoginId";
    public static final String LOGIN_PW_HEADER = "X-Loopers-LoginPw";
    public static final String LDAP_HEADER = "X-Loopers-Ldap";
    public static final String ADMIN_LDAP = "loopers.admin";
}
