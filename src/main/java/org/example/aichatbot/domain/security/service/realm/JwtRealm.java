package org.example.aichatbot.domain.security.service.realm;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.example.aichatbot.domain.security.model.vo.JwtToken;
import org.example.aichatbot.domain.security.service.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Realm for JWT-based authentication
 */
@Slf4j
public class JwtRealm extends AuthorizingRealm {
    private static JwtUtil jwtUtil = new JwtUtil();

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // Currently not needed
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String jwt = (String) token.getPrincipal();
        if (jwt == null) {
            throw new NullPointerException("jwtToken cannot be null");
        }
        // Check validity of JWT
        if (!jwtUtil.isVerify(jwt)) {
            throw new UnknownAccountException();
        }
        // Get username from JWT and perform any necessary operations
        String username = (String) jwtUtil.decode(jwt).get("username");
        log.info("Authenticated user with username: {}", username);
        return new SimpleAuthenticationInfo(jwt, jwt, "JwtRealm");
    }
}
