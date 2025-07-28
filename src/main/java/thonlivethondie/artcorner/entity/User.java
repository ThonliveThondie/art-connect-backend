package thonlivethondie.artcorner.entity;

import jakarta.persistence.*;

/**
 * 테스트 삼아 올려본 엔티티
 * Docker를 통해 MySQL을 띄운 후, JPA를 통해 제대로 동작하는 것을 확인함
 */
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    // OAuth 사용자는 null 가능
    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    private String socialId;
}
