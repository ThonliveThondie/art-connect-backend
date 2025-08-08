package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseEntity;
import thonlivethondie.artconnect.common.Role;
import thonlivethondie.artconnect.common.SocialType;
import thonlivethondie.artconnect.common.UserType;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    // OAuth 사용자는 null 가능
    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "phone_number")
    private String phoneNumber;

    //-- 디자이너인 경우 학력전공, 전문분야를 추가로 필드를 가짐 -- //
    @Column(name = "education")
    private String education;

    @Column(name = "major")
    private String major;

    @Column(name = "specialty")
    private String specialty;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, GOOGLE, NAVER

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType; // 소상공인, 디자이너

    private String socialId;

    private String imageUrl;

    private String refreshToken;

    @Builder
    public User(String email,
                String phoneNumber,
                String password,
                String nickname,
                String education,
                String major,
                String specialty,
                Role role,
                SocialType socialType,
                UserType userType,
                String socialId,
                String imageUrl) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.education = education;
        this.major = major;
        this.specialty = specialty;
        this.nickname = nickname;
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
