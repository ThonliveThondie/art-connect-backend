package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thonlivethondie.artconnect.dto.MyPageResponse;
import thonlivethondie.artconnect.dto.MyPageUpdateRequest;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    // 허용된 전문분야 목록
    private static final List<String> ALLOWED_SPECIALTIES = List.of(
            "로고 디자인",
            "브랜드 디자인",
            "굿즈 디자인",
            "포스터&전단지 디자인",
            "배너&광고 디자인",
            "패키지 디자인",
            "명함&카드&인쇄물 디자인"
    );

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<String> specialty = user.getSpecialty() != null
                ? Arrays.asList(user.getSpecialty().split(","))
                : List.of();

        return MyPageResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .education(user.getEducation())
                .major(user.getMajor())
                .specialty(specialty)
                .imageUrl(user.getImageUrl())
                .build();
    }

    @Transactional
    public void updateMyPage(Long userId, MyPageUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String updatedEducation = request.getEducation() != null ? request.getEducation() : user.getEducation();
        String updatedMajor = request.getMajor() != null ? request.getMajor() : user.getMajor();

        // specialty는 null/빈값이면 기존 값 유지
        String updatedSpecialty;

        // 전문분야 검증
        if (request.getSpecialty() != null && !request.getSpecialty().isEmpty()) {
            if (request.getSpecialty().size() > 3) {
                throw new IllegalArgumentException("전문 분야는 최대 3개까지만 선택 가능합니다.");
            }
            for (String specialty : request.getSpecialty()) {
                if (!ALLOWED_SPECIALTIES.contains(specialty)) {
                    throw new IllegalArgumentException("허용되지 않은 전문 분야입니다: " + specialty);
                }
            }
            updatedSpecialty = String.join(",", request.getSpecialty());
        } else {
            updatedSpecialty = user.getSpecialty();
        }

        // 값 업데이트
        user.updateDesignerInfo(updatedEducation, updatedMajor, updatedSpecialty);

        // phoneNumber는 null이면 유지
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
    }

    @Transactional
    public void updateProfileImage(Long userId, String imageName, String imageUrl, Long imageSize, String imageType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateProfileImage(imageName, imageUrl, imageSize, imageType);
    }
}
