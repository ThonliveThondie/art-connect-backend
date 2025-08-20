package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "work_submission_images")
public class WorkSubmissionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_submission_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_submission_id", nullable = false)
    private WorkSubmission workSubmission;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_name", length = 255)
    private String imageName; // 원본 파일명

    @Column(name = "image_size")
    private Long imageSize; // 파일 크기 (bytes)

    @Column(name = "image_type")
    private String imageType;

    @Builder
    public WorkSubmissionImage(WorkSubmission workSubmission,
                            String imageName,
                            String imageUrl,
                            Long imageSize,
                            String imageType) {
        this.workSubmission = workSubmission;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.imageSize = imageSize;
        this.imageType = imageType;
    }
}
