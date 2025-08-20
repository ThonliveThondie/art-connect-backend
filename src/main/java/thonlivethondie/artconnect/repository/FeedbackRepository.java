package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import thonlivethondie.artconnect.entity.Feedback;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * WorkSubmission ID로 피드백 목록 조회 (작성자 정보 포함)
     */
    @Query("SELECT f FROM Feedback f " +
           "LEFT JOIN FETCH f.author " +
           "WHERE f.workSubmission.id = :workSubmissionId " +
           "ORDER BY f.createDate ASC")
    List<Feedback> findByWorkSubmissionIdWithAuthor(@Param("workSubmissionId") Long workSubmissionId);
    
    /**
     * WorkSubmission ID로 피드백 개수 조회
     */
    long countByWorkSubmissionId(Long workSubmissionId);
}
