package thonlivethondie.artconnect.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thonlivethondie.artconnect.dto.AiProposalDto;
import thonlivethondie.artconnect.dto.AiRecommendationResponseDto;
import thonlivethondie.artconnect.dto.AiRequestDto;
import thonlivethondie.artconnect.dto.RecommendedDesignerDto;
import thonlivethondie.artconnect.service.AiRecommendationService;
import thonlivethondie.artconnect.service.DesignerMatchingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiRecommendationService aiRecommendationService;
    private final DesignerMatchingService designerMatchingService;

    @PostMapping("/recommend")
    public ResponseEntity<AiRecommendationResponseDto> getRecommendation(@RequestBody AiRequestDto request) {
        log.info("AI 추천 요청 시작 - 프롬프트: {}", request.getPrompt());

        try {
            // 1. AI로부터 디자인 제안 받기
            log.info("AI 디자인 제안 서비스 호출 시작");
            AiProposalDto proposal = aiRecommendationService.getProposal(request.getPrompt());
            log.info("AI 디자인 제안 완료 - 방향성: {}", proposal.getDesignDirection());

            // 2. 제안을 기반으로 적합한 디자이너 찾기
            log.info("디자이너 매칭 서비스 호출 시작");
            List<RecommendedDesignerDto> recommendedDesigners = designerMatchingService.findMatchingDesigners(proposal);
            log.info("디자이너 매칭 완료 - 추천 디자이너 수: {}", recommendedDesigners.size());

            // 3. 결과를 통합된 응답 DTO로 조합
            AiRecommendationResponseDto response = new AiRecommendationResponseDto(proposal, recommendedDesigners);

            log.info("AI 추천 요청 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("AI 추천 처리 중 오류 발생", e);
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 던짐
        }
    }
}
