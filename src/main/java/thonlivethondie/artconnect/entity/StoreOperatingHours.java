package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseTimeEntity;
import thonlivethondie.artconnect.common.OperatingHours;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "store_operating_hours")
public class StoreOperatingHours extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_operating_hour_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "operating_hours", nullable = false)
    private OperatingHours operatingHours;

    @Builder
    public StoreOperatingHours(Store store, OperatingHours operatingHours) {
        this.store = store;
        this.operatingHours = operatingHours;
    }
}
