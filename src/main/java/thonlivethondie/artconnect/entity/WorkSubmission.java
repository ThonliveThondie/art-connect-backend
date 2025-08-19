package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "work_submissions")
public class WorkSubmission extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @OneToMany(mappedBy = "workSubmission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkSubmissionImage> workSubmissionImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_request_id", nullable = false)
    private WorkRequest workRequest;

    @OneToMany(mappedBy = "workSubmission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    /**
     * 비즈니스 로직 메서드 - WorkRequest 연관관계를 통한 정보 조회
     */
    public String getStoreName() {
        return this.workRequest.getStore().getStoreName();
    }

    public String getDesignerName() {
        return this.workRequest.getDesigner().getNickname();
    }

    public LocalDate getEndDate() {
        return this.workRequest.getEndDate();
    }

    @Builder
    public WorkSubmission(String comment, WorkRequest workRequest) {
        this.comment = comment;
        this.workRequest = workRequest;
    }

    /**
     * 연관관계 편의 메서드
     */
    public void addWorkSubmissionImage(WorkSubmissionImage workSubmissionImage) {
        this.workSubmissionImages.add(workSubmissionImage);
    }

    public void addFeedback(Feedback feedback) {
        this.feedbacks.add(feedback);
    }
}
