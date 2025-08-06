package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thonlivethondie.artconnect.common.Role;
import thonlivethondie.artconnect.common.exception.BadRequestException;
import thonlivethondie.artconnect.common.exception.ErrorCode;
import thonlivethondie.artconnect.dto.SignUpRequestDto;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequestDto dto) {
        String encodePassword = passwordEncoder.encode(dto.password());

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BadRequestException(ErrorCode.EMAIL_DUPLICATED);
        }

        User user = User.builder()
                .email(dto.email())
                .password(encodePassword)
                .role(Role.USER)
                .userType(dto.userType())
                .build();

        userRepository.save(user);
    }
}
