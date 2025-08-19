package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import thonlivethondie.artconnect.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
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

    //-- 디자이너인 경우 education, major, speciality를 추가로 필드를 가짐 -- //
    @Column(name = "education")
    private String education;

    @Column(name = "major")
    private String major;

    // 전문분야 카테고리들 (최대 3개)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<UserDesignCategory> speciality = new ArrayList<>();

    // 디자인 스타일 카테고리들 (최대 3개)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<UserDesignStyleCategory> designStyleCategories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, GOOGLE, NAVER

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType; // 소상공인, 디자이너

    private String socialId;

    private String refreshToken;

    // 사용자 프로필 이미지
    @Column(name = "image_name")
    private String imageName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_size")
    private Long imageSize;

    @Column(name = "image_type")
    private String imageType;

    // 디자이너인 경우 포트폴리오 목록
    @OneToMany(mappedBy = "designer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Portfolio> portfolios = new ArrayList<>();

    @Builder
    public User(String email,
                String phoneNumber,
                String password,
                String nickname,
                String education,
                String major,
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

    // 전문분야 목록 조회
    public List<DesignCategory> getSelectedSpecialities() {
        return this.speciality.stream()
                .map(UserDesignCategory::getDesignCategory)
                .collect(Collectors.toList());
    }

    // 디자인 스타일 목록 조회
    public List<DesignStyle> getSelectedDesignStyles() {
        return this.designStyleCategories.stream()
                .map(UserDesignStyleCategory::getDesignStyle)
                .collect(Collectors.toList());
    }

    public void updateDesignerInfo(String education, String major) {
        if (education != null && !education.trim().isEmpty()) {
            this.education = education;
        }

        if (major != null && !major.trim().isEmpty()) {
            this.major = major;
        }
    }

    public void updateBasicInfo(String nickname, String phoneNumber) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void updateNickname(String nickname) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
    }

    public void updateProfileImage(String imageName, String imageUrl, Long imageSize, String imageType) {
        if (imageName != null && !imageName.trim().isEmpty()) {
            this.imageName = imageName;
        }
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrl = imageUrl;
        }
        if (imageSize != null) {
            this.imageSize = imageSize;
        }
        if (imageType != null && !imageType.trim().isEmpty()) {
            this.imageType = imageType;
        }
    }
}
