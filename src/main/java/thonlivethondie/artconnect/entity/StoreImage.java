package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "store_images")
public class StoreImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_order")
    private Integer imageOrder;

    @Column(name = "is_main")
    private Boolean isMain;

    @Builder
    public StoreImage(Store store, String imageUrl, Integer imageOrder, Boolean isMain) {
        this.store = store;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
        this.isMain = isMain;
    }

    public void updateImageInfo(String imageUrl, Integer imageOrder, Boolean isMain) {
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (imageOrder != null) {
            this.imageOrder = imageOrder;
        }
        if (isMain != null) {
            this.isMain = isMain;
        }
    }
}
