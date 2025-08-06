package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.Role;
import thonlivethondie.artconnect.common.SocialType;
import thonlivethondie.artconnect.common.UserType;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    // OAuth 사용자는 null 가능
    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, GOOGLE, NAVER

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType; // 소상공인, 대학생·신진 디자이너

    private String socialId;

    private String imageUrl;

    private String refreshToken;

    @Builder
    public User(String email, String password, Role role, SocialType socialType, UserType userType, String socialId, String imageUrl) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.socialType = socialType;
        this.userType = userType;
        this.socialId = socialId;
        this.imageUrl = imageUrl;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
