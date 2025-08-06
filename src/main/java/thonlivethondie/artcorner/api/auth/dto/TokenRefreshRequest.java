package thonlivethondie.artcorner.api.auth.dto;

import lombok.Getter;

@Getter
public class TokenRefreshRequest {
    private String refreshToken;
}