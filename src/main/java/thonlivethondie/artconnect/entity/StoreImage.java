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

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_size")
    private Long imageSize;

    @Column(name = "image_type")
    private String imageType;

    @Builder
    public StoreImage(Store store, String imageName, String imageUrl, Long imageSize, String imageType) {
        this.store = store;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.imageSize = imageSize;
        this.imageType = imageType;
    }

    public void updateImageInfo(String imageName, String imageUrl, Long imageSize, String imageType) {
        if (imageName != null) {
            this.imageName = imageName;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (imageSize != null) {
            this.imageSize = imageSize;
        }
        if (imageType != null) {
            this.imageType = imageType;
        }
    }
}
