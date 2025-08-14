package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseTimeEntity;
import thonlivethondie.artconnect.common.DesignCategory;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "work_request_design_categories")
public class WorkRequestDesignCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_request_design_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_request_id", nullable = false)
    private WorkRequest workRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "design_category", nullable = false)
    private DesignCategory designCategory;

    @Builder
    public WorkRequestDesignCategory(WorkRequest workRequest, DesignCategory designCategory) {
        this.workRequest = workRequest;
        this.designCategory = designCategory;
    }
}
