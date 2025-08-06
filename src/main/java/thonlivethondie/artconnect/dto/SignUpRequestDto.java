package thonlivethondie.artconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import thonlivethondie.artconnect.common.UserType;

public record SignUpRequestDto(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[#@$!%\\?&])[A-Za-z0-9#@$!%\\?&]{8,13}$", message = "비밀번호는 8~13자 영문 대소문자, 숫자, 특수문자를 사용하세요.")
        String password,

        @NotNull(message = "회원 유형은 필수 선택 값입니다.")
        UserType userType
) {
}
