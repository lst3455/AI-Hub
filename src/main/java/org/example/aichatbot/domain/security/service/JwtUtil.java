package org.example.aichatbot.domain.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class JwtUtil {

    // Create default secret key and algorithm for the no-argument constructor
    private static final String defaultBase64EncodedSecretKey = "B*B^";
    private static final SignatureAlgorithm defaultSignatureAlgorithm = SignatureAlgorithm.HS256;

    public JwtUtil() {
        this(defaultBase64EncodedSecretKey, defaultSignatureAlgorithm);
    }

    private final String base64EncodedSecretKey;
    private final SignatureAlgorithm signatureAlgorithm;

    public JwtUtil(String secretKey, SignatureAlgorithm signatureAlgorithm) {
        this.base64EncodedSecretKey = Base64.encodeBase64String(secretKey.getBytes());
        this.signatureAlgorithm = signatureAlgorithm;
    }

    /**
     * This method generates the JWT string.
     * A JWT string has three parts:
     *  1. Header:
     *      - Type of this string, typically "JWT"
     *      - Encryption algorithm, like "HS256" or other algorithms
     *      This is usually fixed, without much variation.
     *  2. Payload:
     *      Usually includes four common standard fields:
     *      - iat: Issued at time, the time the JWT was created
     *      - jti: Unique identifier for the JWT
     *      - iss: Issuer, usually the username or userId
     *      - exp: Expiration time
     */
    public String encode(String issuer, long ttlMillis, Map<String, Object> claims) {
        // "issuer" is the JWT issuer, "ttlMillis" is the token's lifespan, "claims" holds additional non-sensitive information.
        if (claims == null) {
            claims = new HashMap<>();
        }
        long nowMillis = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                // Payload section
                .setClaims(claims)
                // Unique identifier for the JWT, often a UUID
                .setId(UUID.randomUUID().toString())
                // Issued at time
                .setIssuedAt(new Date(nowMillis))
                // Issuer, indicating who the JWT is for (usually the username or userId)
                .setSubject(issuer)
                .signWith(signatureAlgorithm, base64EncodedSecretKey); // This is the algorithm and key for generating the JWT
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis); // Expiration time, using the current time + lifespan
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    // The reverse of encode, accepts a jwtToken and returns fields like username and password. Claims is essentially a map.
    // It retrieves all key-value pairs from the payload section.
    public Claims decode(String jwtToken) {
        // Get the DefaultJwtParser
        return Jwts.parser()
                // Set the secret key for verification
                .setSigningKey(base64EncodedSecretKey)
                // Specify the JWT to parse
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    // Checks if the jwtToken is valid
    public boolean isVerify(String jwtToken) {
        // This is the official verification logic; here we only verify the "algorithm" but more can be added
        Algorithm algorithm = null;
        switch (signatureAlgorithm) {
            case HS256:
                algorithm = Algorithm.HMAC256(Base64.decodeBase64(base64EncodedSecretKey));
                break;
            default:
                throw new RuntimeException("Unsupported algorithm");
        }
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(jwtToken);
        // If verification fails, an exception will be thrown
        // Criteria for validity: 1. The header and payload haven’t been tampered with; 2. The token hasn’t expired
        return true;
    }

}
