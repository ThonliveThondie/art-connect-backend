package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.DesignStyle;
import thonlivethondie.artconnect.common.UserType;
import thonlivethondie.artconnect.dto.*;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.entity.UserDesignCategory;
import thonlivethondie.artconnect.entity.UserDesignStyleCategory;
import thonlivethondie.artconnect.repository.UserDesignCategoryRepository;
import thonlivethondie.artconnect.repository.UserDesignStyleCategoryRepository;
import thonlivethondie.artconnect.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserDesignCategoryRepository userDesignCategoryRepository;
    private final UserDesignStyleCategoryRepository userDesignStyleCategoryRepository;

    @Transactional(readOnly = true)
    public Object getMyPageByUserType(Long userId) {
        User user = findUserById(userId);

        return switch (user.getUserType()) {
            case DESIGNER -> DesignerMyPageResponse.from(user);
            case BUSINESS_OWNER -> BusinessOwnerMyPageResponse.from(
                    user.getNickname(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getImageUrl(),
                    user.getUserType()
            );
        };
    }

    @Transactional
    public void updateDesignerMyPage(Long userId, DesignerMyPageUpdateRequest request) {
        User user = findUserById(userId);

        // 디자이너 타입 검증
        if (user.getUserType() != UserType.DESIGNER) {
            throw new IllegalArgumentException("디자이너가 아닌 사용자는 이 기능을 사용할 수 없습니다.");
        }

        updateDesignerInfo(user, request);
    }

    @Transactional
    public void updateBusinessOwnerMyPage(Long userId, BusinessOwnerMyPageUpdateRequest request) {
        User user = findUserById(userId);

        // 소상공인 타입 검증
        if (user.getUserType() != UserType.BUSINESS_OWNER) {
            throw new IllegalArgumentException("소상공인이 아닌 사용자는 이 기능을 사용할 수 없습니다.");
        }

        user.updateBasicInfo(request.nickName(), request.phoneNumber());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void updateDesignerInfo(User user, DesignerMyPageUpdateRequest request) {
        // 기본 정보 업데이트
        user.updateBasicInfo(request.nickName(), request.phoneNumber());

        // 디자이너 전용 정보(대학, 전공) 업데이트
        user.updateDesignerInfo(request.education(), request.major());

        // 전문분야 업데이트
        updateUserDesignCategories(user, request.designCategories());

        // 디자인 스타일 업데이트
        updateUserDesignStyles(user, request.designStyles());
    }

    /**
     * 사용자 전문분야 업데이트 (기존 데이터 삭제 후 새로 생성)
     */
    private void updateUserDesignCategories(User user, List<DesignCategory> designCategories) {
        // 기존 전문분야 데이터 삭제
        userDesignCategoryRepository.deleteAllByUser(user);

        // 새로운 전문분야 데이터 생성 및 저장
        if (designCategories != null && !designCategories.isEmpty()) {
            List<UserDesignCategory> newCategories = designCategories.stream()
                    .map(category -> UserDesignCategory.of(user, category))
                    .toList();

            userDesignCategoryRepository.saveAll(newCategories);
        }
    }

    /**
     * 사용자 디자인 스타일 업데이트 (기존 데이터 삭제 후 새로 생성)
     */
    private void updateUserDesignStyles(User user, List<DesignStyle> designStyles) {
        // 기존 디자인 스타일 데이터 삭제
        userDesignStyleCategoryRepository.deleteAllByUser(user);

        // 새로운 디자인 스타일 데이터 생성 및 저장
        if (designStyles != null && !designStyles.isEmpty()) {
            List<UserDesignStyleCategory> newStyles = designStyles.stream()
                    .map(style -> UserDesignStyleCategory.of(user, style))
                    .toList();

            userDesignStyleCategoryRepository.saveAll(newStyles);
        }
    }

    /**
     * 프로필 이미지 업데이트 (모든 사용자 타입 공통)
     * TODO: 이미지 업로드 하는 코드 수정 필요
     */
    @Transactional
    public void updateProfileImage(Long userId, String imageName, String imageUrl, Long imageSize, String imageType) {
        User user = findUserById(userId);
        user.updateProfileImage(imageName, imageUrl, imageSize, imageType);
    }
}
