package thonlivethondie.artcorner.entity;

import jakarta.persistence.*;
import lombok.*;
import thonlivethondie.artcorner.oauth.user.OAuth2Provider;

/**
 * 테스트 삼아 올려본 엔티티
 * Docker를 통해 MySQL을 띄운 후, JPA를 통해 제대로 동작하는 것을 확인함
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    private String socialId;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private OAuth2Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Builder
    public User(String email, String password, String nickname,
                String socialId, UserRole role, OAuth2Provider provider, String providerId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.socialId = socialId;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }
}
