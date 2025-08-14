package thonlivethondie.artconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class MyPageUpdateRequest {

    private String phoneNumber;
    private String education;
    private String major;

    @Size(max = 3, message = "전문 분야는 최대 3개까지 선택 가능합니다.")
    private List<String> specialty;
}