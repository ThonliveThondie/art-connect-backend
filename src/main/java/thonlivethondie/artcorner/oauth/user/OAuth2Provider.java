package thonlivethondie.artcorner.oauth.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuth2Provider {
    GOOGLE("google"),
    KAKAO("kakao");

    private final String registrationId;

    public static OAuth2Provider from(String id) {
        return switch (id) {
            case "google" -> GOOGLE;
            case "kakao" -> KAKAO;
            default -> throw new IllegalArgumentException("Unknown provider: " + id);
        };
    }
}
