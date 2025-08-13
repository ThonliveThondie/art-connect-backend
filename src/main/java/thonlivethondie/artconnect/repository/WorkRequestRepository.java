package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thonlivethondie.artconnect.common.WorkRequestStatus;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.entity.WorkRequest;

import java.util.List;

@Repository
public interface WorkRequestRepository extends JpaRepository<WorkRequest, Long> {
    // 디자이너가 받은 의뢰서 목록 (최신순)
    List<WorkRequest> findByDesignerOrderByCreateDateDesc(User designer);

    // 소상공인이 보낸 의뢰서 목록 (최신순)
    List<WorkRequest> findByBusinessOwnerOrderByCreateDateDesc(User businessOwner);

    // 특정 상태의 의뢰서 조회
    List<WorkRequest> findByDesignerAndStatusOrderByCreateDateDesc(User designer, WorkRequestStatus status);
}
