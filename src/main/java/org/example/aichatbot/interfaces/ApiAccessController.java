package org.example.aichatbot.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.example.aichatbot.domain.security.service.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class ApiAccessController {

    @RequestMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize(String username, String password) {
        Map<String, String> map = new HashMap<>();

        // Simulate username and password validation
        if (!"test".equals(username) || !"test".equals(password)) {
            map.put("msg", "Username or password is incorrect");
            return ResponseEntity.ok(map);
        }

        // If validation succeeds, generate a token
        JwtUtil jwtUtil = new JwtUtil();
        Map<String, Object> claim = new HashMap<>();
        claim.put("username", username);
        String jwtToken = jwtUtil.encode(username, 5 * 60 * 1000, claim);

        map.put("msg", "Authorization successful");
        map.put("token", jwtToken);

        // Return the token
        return ResponseEntity.ok(map);
    }

    /**
     * http://localhost:8080/verify?token=
     */
    @RequestMapping("/verify")
    public ResponseEntity<String> verify(String token) {
        log.info("Verifying token: {}", token);
        return ResponseEntity.status(HttpStatus.OK).body("verify success!");
    }

    @RequestMapping("/success")
    public String success() {
        return "test success";
    }
}
