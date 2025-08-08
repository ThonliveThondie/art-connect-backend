package thonlivethondie.artconnect.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import thonlivethondie.artconnect.common.Role;

import java.util.Collection;
import java.util.Map;

/**
 * DefaultOAuth2User를 상속하고, email과 role 필드를 추가로 가진다.
 */
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private Long userId;
    private String email;
    private Role role;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attribute, String nameAttributeKey,
                            Long userId, String email, Role role) {
        super(authorities, attribute, nameAttributeKey);
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}
