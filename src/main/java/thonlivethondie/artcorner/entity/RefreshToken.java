package thonlivethondie.artcorner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private Long userId; // user_id가 PK (1:1 관계)

    @Column(nullable = false, length = 500)
    private String token;

    public void updateToken(String newToken) {
        this.token = newToken;
    }

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder
    public RefreshToken(Long userId, String token) {
        this.userId = userId;
        this.token = token;
        this.expiryDate = LocalDateTime.now().plusWeeks(2); // 2주 유효
    }
}