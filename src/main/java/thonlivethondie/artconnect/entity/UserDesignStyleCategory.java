package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseTimeEntity;
import thonlivethondie.artconnect.common.DesignStyle;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_design_style_categories")
public class UserDesignStyleCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_design_style_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "design_style", nullable = false)
    private DesignStyle designStyle;

    @Builder
    public UserDesignStyleCategory(User user, DesignStyle designStyle) {
        this.user = user;
        this.designStyle = designStyle;
    }

    public static UserDesignStyleCategory of(User user, DesignStyle designStyle) {
        return UserDesignStyleCategory.builder()
                .user(user)
                .designStyle(designStyle)
                .build();
    }
}