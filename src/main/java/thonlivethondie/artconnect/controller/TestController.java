package thonlivethondie.artconnect.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    /**
     * 인증된 사용자 정보를 반환하는 테스트 엔드포인트
     * 소셜 로그인 사용자의 인증 메커니즘 테스트용
     */
    @GetMapping("/auth-info")
    public ResponseEntity<Map<String, Object>> getAuthInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 데이터베이스에서 사용자 정보 조회
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            
            response.put("success", true);
            response.put("message", "인증 성공!");
            response.put("email", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());
            response.put("isAuthenticated", authentication.isAuthenticated());
            
            if (user != null) {
                response.put("userInfo", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "socialType", user.getSocialType(),
                    "socialId", user.getSocialId(),
                    "imageUrl", user.getImageUrl(),
                    "hasPassword", user.getPassword() != null
                ));
            }
            
            log.info("인증된 사용자 정보: {}", userDetails.getUsername());
            log.info("사용자 권한: {}", userDetails.getAuthorities());
            
        } else {
            response.put("success", false);
            response.put("message", "인증되지 않은 사용자");
            response.put("isAuthenticated", false);
            
            log.warn("인증되지 않은 사용자 접근");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 간단한 인증 테스트 엔드포인트
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "인증된 사용자만 접근 가능한 엔드포인트");
        response.put("email", authentication != null ? authentication.getName() : "Unknown");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 역할별 접근 테스트
     */
    @GetMapping("/role-test")
    public ResponseEntity<Map<String, Object>> roleTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "역할 테스트 성공");
        response.put("email", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }
} 