package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thonlivethondie.artconnect.entity.WorkSubmissionImage;

import java.util.List;

@Repository
public interface WorkSubmissionImageRepository extends JpaRepository<WorkSubmissionImage, Long> {
    
    /**
     * WorkSubmission ID로 이미지 목록 조회
     */
    List<WorkSubmissionImage> findByWorkSubmissionId(Long workSubmissionId);
}
