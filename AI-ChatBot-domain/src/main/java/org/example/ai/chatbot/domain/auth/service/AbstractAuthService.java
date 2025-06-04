package org.example.ai.chatbot.domain.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.example.ai.chatbot.domain.auth.model.entity.AuthStateEntity;
import org.example.ai.chatbot.domain.auth.model.valobj.AuthTypeVO;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class for authentication service, handling user login,
 * token generation, and token validation for authentication purposes.
 */
@Slf4j
public abstract class AbstractAuthService implements IAuthService {

    /** SecretKey should be replaced with your own and ideally provided via configuration. */
    private static final String defaultBase64EncodedSecretKey = "B*B^D%fe";
    private final String base64EncodedSecretKey = Base64.encodeBase64String(defaultBase64EncodedSecretKey.getBytes());
    private final Algorithm algorithm = Algorithm.HMAC256(Base64.decodeBase64(Base64.encodeBase64String(defaultBase64EncodedSecretKey.getBytes())));

    /**
     * Handles the login process based on a provided verification code.
     * Validates the code, checks for success, and generates a token upon successful authentication.
     *
     * @param code The verification code provided by the user.
     * @return An AuthStateEntity containing the result of the authentication and a token if successful.
     */
    @Override
    public AuthStateEntity doLogin(String code, String openId) {

        // 1. If the code is not a valid 4-digit numeric string, return an invalid code response.
        if (!code.matches("\\d{4}")) {
            log.info("Authentication failed, invalid verification code: {}", code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVO.A0002.getCode())
                    .info(AuthTypeVO.A0002.getInfo())
                    .build();
        }

        // 2. Validate the code, return immediately if not successful.
        AuthStateEntity authStateEntity = this.checkCode(code);
        if (!authStateEntity.getCode().equals(AuthTypeVO.A0000.getCode())) {
            return authStateEntity;
        }

        // 3. Generate and return a token upon successful authentication.
        if (!authStateEntity.getOpenId().equals(openId)) {
            log.info("Authentication failed, invalid verification code: {}", code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVO.A0003.getCode())
                    .info(AuthTypeVO.A0003.getInfo())
                    .build();
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("openId", authStateEntity.getOpenId());
        String token = encode(authStateEntity, 7 * 24 * 60 * 60 * 1000, claims);
        authStateEntity.setToken(token);

        return authStateEntity;
    }

    /**
     * Abstract method to check the provided verification code.
     * Implemented in subclasses to define specific code validation logic.
     *
     * @param code The verification code to check.
     * @return An AuthStateEntity with the result of the code validation.
     */
    protected abstract AuthStateEntity checkCode(String code);

    /**
     * Generates a JWT string.
     * The JWT string includes three parts:
     * 1. Header - Defines the type of the token (JWT) and the algorithm used for signing (e.g., HS256).
     * 2. Payload - Includes common fields such as:
     *    - iat: Issued at timestamp
     *    - jti: Unique identifier for the JWT
     *    - iss: Issuer (typically a username or userId)
     *    - exp: Expiration time
     *
     * @param authStateEntity The authStateEntity.
     * @param ttlMillis Token lifetime in milliseconds.
     * @param claims Additional claims to include in the token payload.
     * @return A compact JWT string.
     */
    protected String encode(AuthStateEntity authStateEntity, long ttlMillis, Map<String, Object> claims) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        long nowMillis = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)                          // Payload section
                .setId(UUID.randomUUID().toString())        // Unique identifier for the JWT
                .setIssuedAt(new Date(nowMillis))           // Issued at timestamp
                .setSubject(authStateEntity.getOpenId())                         // Issuer of the token
                .signWith(SignatureAlgorithm.HS256, base64EncodedSecretKey); // Signing algorithm and secret key

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);                 // Expiration timestamp
            builder.setExpiration(exp);
            authStateEntity.setExpireDate(exp);
        }
        return builder.compact();
    }

    /**
     * Decodes a JWT token to retrieve claims (payload data) as a map.
     * The claims section contains all key-value pairs stored in the token.
     *
     * @param jwtToken The JWT token to decode.
     * @return A Claims object containing the decoded payload data.
     */
    protected Claims decode(String jwtToken) {
        return Jwts.parser()
                .setSigningKey(base64EncodedSecretKey)      // Set signing key
                .parseClaimsJws(jwtToken)                   // Parse and validate the token
                .getBody();
    }

    /**
     * Verifies the validity of a JWT token.
     * Ensures the token has not been tampered with and has not expired.
     *
     * @param jwtToken The JWT token to verify.
     * @return True if the token is valid, false otherwise.
     */
    protected boolean isVerify(String jwtToken) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(jwtToken);
            // Verification failure throws an exception.
            // Valid if: 1. Header and payload are unmodified, 2. Token has not expired
            return true;
        } catch (Exception e) {
            log.error("JWT verification error", e);
            return false;
        }
    }

}
