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

    @Column(name = "store_type", length = 50)
    private String storeType;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // 매장 운영시간
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreOperatingHours> storeOperatingHours = new ArrayList<>();

    // 매장 정보 이미지들
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> storeImages = new ArrayList<>();

    // 매장의 작업의뢰서들
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkRequest> workRequests = new ArrayList<>();

    @Builder
    public Store(User user, String storeName, String storeType, String phoneNumber) {

        // 소상공인만 매장을 생성할 수 있는 검증 로직
        if (user.getUserType() != UserType.BUSINESS_OWNER) {
            throw new IllegalArgumentException("매장은 소상공인만 생성할 수 있습니다.");
        }

        this.user = user;
        this.storeName = storeName;
        this.storeType = storeType;
        this.phoneNumber = phoneNumber;
    }

    public void updateStoreInfo(String storeName, String storeType, String phoneNumber, List<StoreOperatingHours> operatingHours) {
        if (storeName != null) {
            this.storeName = storeName;
            this.user.updateNickname(storeName);
        }
        if (storeType != null) {
            this.storeType = storeType;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (operatingHours != null) {
            updateOperatingHours(operatingHours);
        }
    }

    // 운영시간 업데이트 헬퍼 메서드
    private void updateOperatingHours(List<StoreOperatingHours> newOperatingHours) {
        // 기존 운영시간 모두 삭제
        this.storeOperatingHours.clear();
        
        // 새로운 운영시간 추가
        for (StoreOperatingHours operatingHour : newOperatingHours) {
            StoreOperatingHours newOperatingHour = StoreOperatingHours.builder()
                    .store(this)
                    .operatingHours(operatingHour.getOperatingHours())
                    .build();
            this.storeOperatingHours.add(newOperatingHour);
        }
    }

    // 초기 운영시간 설정 메서드 (Store 저장 후 호출)
    public void setOperatingHours(List<StoreOperatingHours> operatingHours) {
        if (operatingHours != null) {
            for (StoreOperatingHours operatingHour : operatingHours) {
                StoreOperatingHours newOperatingHour = StoreOperatingHours.builder()
                        .store(this)
                        .operatingHours(operatingHour.getOperatingHours())
                        .build();
                this.storeOperatingHours.add(newOperatingHour);
            }
        }
    }
}
