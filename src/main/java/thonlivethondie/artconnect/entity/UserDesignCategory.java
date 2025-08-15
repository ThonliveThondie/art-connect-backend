package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.DesignCategory;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_design_categories")
public class UserDesignCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_design_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "design_category", nullable = false)
    private DesignCategory designCategory;

    @Builder
    public UserDesignCategory(User user, DesignCategory designCategory) {
        this.user = user;
        this.designCategory = designCategory;
    }

    public static UserDesignCategory of(User user, DesignCategory designCategory) {
        return UserDesignCategory.builder()
                .user(user)
                .designCategory(designCategory)
                .build();
    }
}