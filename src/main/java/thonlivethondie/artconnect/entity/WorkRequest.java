package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseEntity;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.WorkRequestStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "work_requests")
public class WorkRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_request_id")
    private Long id;

    // 의뢰자 (소상공인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_owner_id", nullable = false)
    private User businessOwner;

    // 수신자 (디자이너)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id", nullable = false)
    private User designer;

    /**
     * 1. 기본 정보
     */
    // 프로젝트명
    @Column(name = "project_title", nullable = false)
    private String projectTitle;

    // 업체명 (매장명 가져오기)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 희망 납기일
    @Column(name = "end_date")
    private LocalDate endDate;

    // 제안 금액
    @Column(name = "budget")
    private Long budget;

    /**
     * 2. 브랜드 소개 및 프로젝트 배경
     */
    @Column(name = "product_service", columnDefinition = "TEXT")
    private String productService;

    @Column(name = "target_customers")
    private String targetCustomers;

    @Column(name = "now_status")
    private String nowStatus;

    @Column(name = "goal")
    private String goal;

    /**
     * 3. 요청 디자인 항목
     */
    @OneToMany(mappedBy = "workRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkRequestDesignCategory> designCategories = new ArrayList<>();

    /**
     * 4. 참고 자료 (선택)
     */
    @Column(name = "additional_description", columnDefinition = "TEXT")
    private String additionalDescription;

    @OneToMany(mappedBy = "workRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkRequestImage> workRequestImages = new ArrayList<>();

    /**
     * 5. 기타 요구사항 (선택)
     */
    @Column(name = "additional_requirement", columnDefinition = "TEXT")
    private String additionalRequirement;

    // 의뢰서 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkRequestStatus status;

    @Builder
    public WorkRequest(User businessOwner,
                       User designer,
                       Store store,
                       String projectTitle,
                       LocalDate endDate,
                       Long budget,
                       String productService,
                       String targetCustomers,
                       String nowStatus,
                       String goal,
                       String additionalDescription,
                       String additionalRequirement,
                       WorkRequestStatus status) {
        this.businessOwner = businessOwner;
        this.designer = designer;
        this.store = store;
        this.projectTitle = projectTitle;
        this.endDate = endDate;
        this.budget = budget;
        this.productService = productService;
        this.targetCustomers = targetCustomers;
        this.nowStatus = nowStatus;
        this.goal = goal;
        this.additionalDescription = additionalDescription;
        this.additionalRequirement = additionalRequirement;
        this.status = status != null ? status : WorkRequestStatus.PROPOSAL;
    }

    /**
     * 연관관계 편의 메서드
     */
    // 디자인 카테고리 일괄 설정
    public void setDesignCategories(List<DesignCategory> categories) {
        if (categories.size() > 3) {
            throw new IllegalArgumentException("최대 3개의 디자인 카테고리만 선택할 수 있습니다.");
        }

        // 기존 카테고리 모두 제거
        this.designCategories.clear();

        // 새 카테고리들 추가
        for (DesignCategory category : categories) {
            this.addDesignCategory(category);
        }
    }

    // 디자인 카테고리 추가 메서드
    public void addDesignCategory(DesignCategory category) {
        if (this.designCategories.size() >= 3) {
            throw new IllegalArgumentException("최대 3개의 디자인 카테고리만 선택할 수 있습니다.");
        }

        // 중복 체크
        boolean exists = this.designCategories.stream()
                .anyMatch(dc -> dc.getDesignCategory() == category);

        if (exists) {
            throw new IllegalArgumentException("이미 선택된 디자인 카테고리입니다.");
        }

        WorkRequestDesignCategory workRequestDesignCategory =
                WorkRequestDesignCategory.builder()
                        .workRequest(this)
                        .designCategory(category)
                        .build();

        this.designCategories.add(workRequestDesignCategory);
    }

    // 디자인 카테고리 제거 메서드
    public void removeDesignCategory(DesignCategory category) {
        this.designCategories.removeIf(dc -> dc.getDesignCategory() == category);
    }

    // 디자인 카테고리 목록 조회
    public List<DesignCategory> getSelectedDesignCategories() {
        return this.designCategories.stream()
                .map(WorkRequestDesignCategory::getDesignCategory)
                .collect(Collectors.toList());
    }

    // 상태 변경 메서드
    public void updateStatus(WorkRequestStatus status) {
        this.status = status;
    }

    // 제안 수락 메서드 (PROPOSAL -> PENDING)
    public void acceptProposal() {
        if (this.status != WorkRequestStatus.PROPOSAL) {
            throw new IllegalStateException("제안 상태가 아닌 작업의뢰서는 수락할 수 없습니다.");
        }
        this.status = WorkRequestStatus.PENDING;
    }
}
