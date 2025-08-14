package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseEntity;
import thonlivethondie.artconnect.common.UserType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stores")
public class Store extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "operating_hours", length = 500)
    private String operatingHours;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> storeImages = new ArrayList<>();

    @Builder
    public Store(User user, String storeName, String address, String phoneNumber, String operatingHours) {

        // 소상공인만 매장을 생성할 수 있는 검증 로직
        if (user.getUserType() != UserType.BUSINESS_OWNER) {
            throw new IllegalArgumentException("매장은 소상공인만 생성할 수 있습니다.");
        }

        this.user = user;
        this.storeName = storeName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.operatingHours = operatingHours;
    }

    public void updateStoreInfo(String storeName, String address, String phoneNumber, String operatingHours) {
        if (storeName != null) {
            this.storeName = storeName;
            this.user.updateNickname(storeName);
        }
        if (address != null) {
            this.address = address;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (operatingHours != null) {
            this.operatingHours = operatingHours;
        }
    }
}
