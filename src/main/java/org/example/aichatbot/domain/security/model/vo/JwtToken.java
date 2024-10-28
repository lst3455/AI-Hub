package org.example.aichatbot.domain.security.model.vo;

import org.apache.shiro.authc.AuthenticationToken;

public class JwtToken implements AuthenticationToken {

    private String jwt;

    public JwtToken(String jwt) {
        this.jwt = jwt;
    }

    /**
     * similar to account
     */
    @Override
    public Object getPrincipal() {
        return jwt;
    }

    /**
     * similar to password
     */
    @Override
    public Object getCredentials() {
        return jwt;
    }

}

