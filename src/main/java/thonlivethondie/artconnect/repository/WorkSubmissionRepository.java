package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import thonlivethondie.artconnect.entity.WorkSubmission;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkSubmissionRepository extends JpaRepository<WorkSubmission, Long> {
    
    /**
     * 특정 WorkRequest의 모든 WorkSubmission 조회 (이미지 포함)
     * 오래된 시안부터 최신 시안 순으로 정렬
     */
    @Query("SELECT DISTINCT ws FROM WorkSubmission ws " +
           "LEFT JOIN FETCH ws.workSubmissionImages " +
           "WHERE ws.workRequest.id = :workRequestId " +
           "ORDER BY ws.createDate ASC")
    List<WorkSubmission> findByWorkRequestIdWithImagesAndFeedbacks(@Param("workRequestId") Long workRequestId);
    
    /**
     * WorkSubmission ID로 조회 (이미지 포함)
     */
    @Query("SELECT DISTINCT ws FROM WorkSubmission ws " +
           "LEFT JOIN FETCH ws.workSubmissionImages " +
           "WHERE ws.id = :submissionId")
    Optional<WorkSubmission> findByIdWithImagesAndFeedbacks(@Param("submissionId") Long submissionId);
    
    /**
     * WorkRequest ID로 WorkSubmission 목록 조회 (기본)
     * 오래된 시안부터 최신 시안 순으로 정렬
     */
    List<WorkSubmission> findByWorkRequestIdOrderByCreateDateAsc(Long workRequestId);
}
