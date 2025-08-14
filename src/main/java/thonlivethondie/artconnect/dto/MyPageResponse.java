package thonlivethondie.artconnect.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponse {
    private String nickname;         // 변경 불가
    private String email;            // 변경 불가
    private String phoneNumber;      // 변경 가능
    private String education;           // 변경 가능
    private String major;            // 변경 가능
    private List<String> specialty; // 최대 3개 선택 가능
    private String imageUrl;  // 프로필 이미지 경로
}
