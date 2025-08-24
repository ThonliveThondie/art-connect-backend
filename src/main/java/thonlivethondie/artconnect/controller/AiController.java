package thonlivethondie.artconnect.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thonlivethondie.artconnect.dto.*;
import thonlivethondie.artconnect.service.AiRecommendationService;
import thonlivethondie.artconnect.service.DesignerMatchingService;
import thonlivethondie.artconnect.service.DesignerMatchingSessionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiRecommendationService aiRecommendationService;
    private final DesignerMatchingService designerMatchingService;
    private final DesignerMatchingSessionService sessionService;

    @PostMapping("/recommend")
    public ResponseEntity<AiRecommendationResponseDto> getRecommendation(@RequestBody AiRequestDto request) {
        log.info("AI 추천 요청 시작 - 프롬프트: {}", request.getPrompt());

        try {
            // 1. AI로부터 디자인 제안 받기
            log.info("AI 디자인 제안 서비스 호출 시작");
            AiProposalDto proposal = aiRecommendationService.getProposal(request.getPrompt());
            log.info("AI 디자인 제안 완료 - 방향성: {}", proposal.getDesignDirection());

            // 2. 제안을 기반으로 점수가 매겨진 디자이너 찾기 (최대 10명)
            log.info("점수 기반 디자이너 매칭 서비스 호출 시작");
            List<ScoredDesignerDto> scoredDesigners = designerMatchingService.findScoredMatchingDesigners(proposal);
            log.info("점수 기반 디자이너 매칭 완료 - 매칭된 디자이너 수: {}", scoredDesigners.size());

            // 3. 세션 생성 및 저장
            DesignerMatchingSessionDto session = new DesignerMatchingSessionDto();
            session.setProposal(proposal);
            session.setAllMatchingDesigners(scoredDesigners);
            
            String sessionId = sessionService.createSession(session);
            log.info("디자이너 매칭 세션 생성 완료 - 세션 ID: {}", sessionId);

            // 4. 초기 추천 디자이너 2명 선택 (1등, 2등)
            List<RecommendedDesignerDto> initialRecommendations = sessionService.getInitialRecommendations(sessionId);
            log.info("초기 디자이너 추천 완료 - 추천 디자이너 수: {}", initialRecommendations.size());

            // 5. 결과를 통합된 응답 DTO로 조합
            AiRecommendationResponseDto response = new AiRecommendationResponseDto(proposal, initialRecommendations, sessionId);

            log.info("AI 추천 요청 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("AI 추천 처리 중 오류 발생", e);
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 던짐
        }
    }

    /**
     * 세션 기반으로 새로운 디자이너 추천을 받습니다 (새로고침 기능 - Path Variable 방식).
     *
     * @param sessionId 기존 매칭 세션 ID
     * @return 새로고침된 디자이너 추천 목록
     */
    @PostMapping("/refresh/{sessionId}")
    public ResponseEntity<List<RecommendedDesignerDto>> refreshRecommendations(@PathVariable String sessionId) {
        log.info("디자이너 추천 새로고침 요청 (Path Variable) - 세션 ID: {}", sessionId);

        try {
            // 세션에서 새로고침된 디자이너 추천 가져오기
            List<RecommendedDesignerDto> refreshedRecommendations = sessionService.getRefreshedRecommendations(sessionId);
            
            if (refreshedRecommendations.isEmpty()) {
                log.warn("새로고침할 디자이너가 없음 - 세션 ID: {}", sessionId);
                return ResponseEntity.notFound().build();
            }

            log.info("디자이너 추천 새로고침 완료 - 세션 ID: {}, 추천 디자이너 수: {}", 
                    sessionId, refreshedRecommendations.size());
            
            return ResponseEntity.ok(refreshedRecommendations);

        } catch (Exception e) {
            log.error("디자이너 추천 새로고침 중 오류 발생 - 세션 ID: {}", sessionId, e);
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 던짐
        }
    }

    /**
     * 세션 기반으로 새로운 디자이너 추천을 받습니다 (새로고침 기능 - Request Body 방식).
     *
     * @param request 새로고침 요청 DTO
     * @return 새로고침된 디자이너 추천 목록
     */
    @PostMapping("/refresh")
    public ResponseEntity<List<RecommendedDesignerDto>> refreshRecommendationsWithBody(@RequestBody RefreshRequestDto request) {
        log.info("디자이너 추천 새로고침 요청 (Request Body) - 세션 ID: {}", request.getSessionId());

        try {
            // 세션에서 새로고침된 디자이너 추천 가져오기
            List<RecommendedDesignerDto> refreshedRecommendations = sessionService.getRefreshedRecommendations(request.getSessionId());
            
            if (refreshedRecommendations.isEmpty()) {
                log.warn("새로고침할 디자이너가 없음 - 세션 ID: {}", request.getSessionId());
                return ResponseEntity.notFound().build();
            }

            log.info("디자이너 추천 새로고침 완료 - 세션 ID: {}, 추천 디자이너 수: {}", 
                    request.getSessionId(), refreshedRecommendations.size());
            
            return ResponseEntity.ok(refreshedRecommendations);

        } catch (Exception e) {
            log.error("디자이너 추천 새로고침 중 오류 발생 - 세션 ID: {}", request.getSessionId(), e);
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 던짐
        }
    }

    /**
     * 세션 정보를 조회합니다 (디버깅 및 모니터링용).
     *
     * @param sessionId 조회할 세션 ID
     * @return 세션 정보
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<DesignerMatchingSessionDto> getSessionInfo(@PathVariable String sessionId) {
        log.info("세션 정보 조회 요청 - 세션 ID: {}", sessionId);

        try {
            DesignerMatchingSessionDto session = sessionService.getSession(sessionId);
            
            if (session == null) {
                log.warn("세션을 찾을 수 없음 - 세션 ID: {}", sessionId);
                return ResponseEntity.notFound().build();
            }

            log.info("세션 정보 조회 완료 - 세션 ID: {}", sessionId);
            return ResponseEntity.ok(session);

        } catch (Exception e) {
            log.error("세션 정보 조회 중 오류 발생 - 세션 ID: {}", sessionId, e);
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 던짐
        }
    }

    @PostMapping("/generate-work-request/{sessionId}")
    public ResponseEntity<WorkRequestCreateRequestDto> generateWorkRequestFromSession(
            @PathVariable String sessionId) {

        log.info("세션 기반 작업의뢰서 자동 생성 요청 - 세션 ID: {}", sessionId);

        try {
            // 세션 ID를 기반으로 AI 제안 정보를 활용하여 작업의뢰서 생성
            WorkRequestCreateRequestDto workRequest = aiRecommendationService.generateWorkRequestFromSession(sessionId);

            log.info("작업의뢰서 자동 생성 완료 - 세션 ID: {}, 프로젝트명: {}", sessionId, workRequest.projectTitle());

            return ResponseEntity.ok(workRequest);
        } catch (Exception e) {
            log.error("작업의뢰서 자동 생성 중 오류 발생 - 세션 ID: {}", sessionId, e);
            throw e;
        }
    }
}
