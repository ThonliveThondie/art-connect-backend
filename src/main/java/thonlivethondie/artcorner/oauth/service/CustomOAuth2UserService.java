package thonlivethondie.artcorner.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thonlivethondie.artcorner.entity.User;
import thonlivethondie.artcorner.entity.UserRole;
import thonlivethondie.artcorner.jwt.JwtTokenPair;
import thonlivethondie.artcorner.jwt.JwtUserClaim;
import thonlivethondie.artcorner.oauth.user.OAuth2Provider;
import thonlivethondie.artcorner.oauth.user.OAuth2UserInfo;
import thonlivethondie.artcorner.repository.UserRepository;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        Map<String, Object> attributes = super.loadUser(request).getAttributes();
        String registrationId = request.getClientRegistration().getRegistrationId();
        String userNameAttributeName = request.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        String providerId = attributes.get(userNameAttributeName).toString();

        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, attributes);
        User user = getOrSave(userInfo, registrationId, providerId);

        return new OAuth2UserPrincipal(user, attributes, userNameAttributeName);
    }

    private User getOrSave(OAuth2UserInfo userInfo, String registrationId, String providerId) {
        OAuth2Provider provider = OAuth2Provider.from(registrationId);

        return userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(userInfo.email())
                                .nickname(userInfo.name()) // ✅ 수정됨
                                .role(UserRole.USER)
                                .provider(provider)
                                .providerId(providerId)
                                .build()
                ));

    }
}
