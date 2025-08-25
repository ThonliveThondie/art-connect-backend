package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import thonlivethondie.artconnect.dto.AiProposalDto;
import thonlivethondie.artconnect.dto.DesignerMatchingSessionDto;
import thonlivethondie.artconnect.dto.WorkRequestCreateRequestDto;
import thonlivethondie.artconnect.common.DesignCategory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final AnthropicChatModel anthropicChatModel;
    private final DesignerMatchingSessionService sessionService;

    /**
     * 사용자 프롬프트를 기반으로 AI로부터 디자인 제안을 받아옵니다.
     *
     * @param userPrompt 사용자가 입력한 디자인 요청 내용
     * @return AI가 생성한 디자인 제안 정보
     */
    public AiProposalDto getProposal(String userPrompt) {
        log.info("AI 디자인 제안 요청 시작 - 사용자 프롬프트: {}", userPrompt);

        try {
            // BeanOutputConverter를 사용하여 JSON 응답을 AiProposalDto로 자동 변환
            BeanOutputConverter<AiProposalDto> outputConverter = new BeanOutputConverter<>(AiProposalDto.class);

            // 시스템 프롬프트 구성 - AI가 디자인 컨설턴트 역할을 하도록 지시
            String systemPromptText = createSystemPrompt(outputConverter.getFormat());

            // 메시지 리스트 구성
            List<Message> messages = List.of(
                    new SystemMessage(systemPromptText),
                    new UserMessage(userPrompt)
            );

            // AI에게 프롬프트 전송
            Prompt prompt = new Prompt(messages);
            String aiResponse = anthropicChatModel.call(prompt).getResult().getOutput().getContent();

            log.info("AI 응답 수신 완료");
            log.debug("AI 응답 내용: {}", aiResponse);

            // JSON 응답을 AiProposalDto 객체로 변환
            AiProposalDto proposal = outputConverter.convert(aiResponse);

            log.info("AI 디자인 제안 생성 완료 - 디자인 방향성: {}", proposal.getDesignDirection());
            return proposal;

        } catch (Exception e) {
            log.error("AI 디자인 제안 생성 중 오류 발생", e);
            throw new RuntimeException("AI 디자인 제안 생성에 실패했습니다.", e);
        }
    }

    /**
     * AI가 디자인 컨설턴트 역할을 하도록 하는 시스템 프롬프트를 생성합니다.
     *
     * @param format BeanOutputConverter에서 제공하는 JSON 형식 정보
     * @return 완성된 시스템 프롬프트
     */
    private String createSystemPrompt(String format) {
        return String.format("""
                당신은 전문적인 디자인 컨설턴트입니다. 클라이언트의 요청을 분석하여 최적의 디자인 방향성을 제안해주세요.
                
                다음 사항들을 고려하여 분석해주세요:
                1. 클라이언트의 비즈니스 특성과 목표
                2. 타겟 고객층의 특성과 선호도
                3. 현재 트렌드와 시장 상황
                4. 브랜드 아이덴티티 구축 방향
                
                응답은 반드시 다음 JSON 형식으로만 제공해주세요:
                %s
                
                각 필드에 대한 설명:
                - designDirection: 추천하는 디자인 방향성 (예: "모던하고 심플한 미니멀 스타일")
                - targetCustomer: 분석된 주요 타겟 고객층 (예: "20-30대 직장인 여성")
                - requiredDesigns: 필요한 디자인 요소들 (예: "로고, 명함, 브로셔, 웹사이트")
                
                전문적이고 구체적인 조언을 제공하되, 각 필드는 간결하고 명확하게 작성해주세요.
                """, format);
    }

    public WorkRequestCreateRequestDto generateWorkRequestFromSession(String sessionId) {
        log.info("세션 기반 AI 작업의뢰서 생성 요청 시작 - 세션 ID: {}", sessionId);

        try {
            DesignerMatchingSessionDto session = sessionService.getSession(sessionId);

            if (session == null) {
                throw new IllegalArgumentException("세션을 찾을 수 없습니다." + sessionId);
            }

            AiProposalDto proposal = session.getProposal();
            if (proposal == null) {
                throw new IllegalArgumentException("세션에 AI 제안 정보가 없습니다." + sessionId);
            }

            // BeanOutputConverter를 사용하여 JSON 응답을 WorkRequestCreateRequestDto로 자동 변환
            BeanOutputConverter<WorkRequestCreateRequestDto> outputConverter =
                    new BeanOutputConverter<>(WorkRequestCreateRequestDto.class);

            // 작업의뢰서 생성을 위한 시스템 프롬프트 구성
            String systemPromptText = createWorkRequestSystemPrompt(outputConverter.getFormat());

            // AI 제안을 기반으로 한 상세 프롬프트 생성
            String aiPrompt = createWorkRequestPromptFromProposal(proposal);

            // 메시지 리스트 구성
            List<Message> messages = List.of(
                    new SystemMessage(systemPromptText),
                    new UserMessage(aiPrompt)
            );

            // AI에게 프롬프트 전송
            Prompt prompt = new Prompt(messages);
            String aiResponse = anthropicChatModel.call(prompt).getResult().getOutput().getContent();

            log.info("AI 작업의뢰서 응답 수신 완료 {}", aiResponse);

            // JSON 응답 검증 및 정리
            String cleanedResponse = cleanJsonResponse(aiResponse);
            log.info("정리된 응답: {}", cleanedResponse);

            // JSON 응답을 WorkRequestCreateRequestDto 객체로 변환
            WorkRequestCreateRequestDto workRequest = outputConverter.convert(cleanedResponse);
            
            log.info("AI 작업의뢰서 생성 완료 - 프로젝트명: {}", workRequest.projectTitle());
            return workRequest;
        } catch (Exception e) {
            log.error("AI 작업의뢰서 생성 중 오류 발생 - 세션 ID: {}", sessionId, e);
            throw new RuntimeException("AI 작업의뢰서 생성에 실패했습니다.", e);
        }
    }

    private String createWorkRequestPromptFromProposal(AiProposalDto aiProposal) {
        return """
                다음 AI 제안을 바탕으로 명확한 작업의뢰서를 작성해주세요:
                
                - 디자인 방향: %s
                - 타겟 고객: %s
                - 필요한 디자인: %s
                
                위 내용을 반영하여 각 필드를 작성해주세요.
                """.formatted(
                aiProposal.getDesignDirection(),
                aiProposal.getTargetCustomer(),
                aiProposal.getRequiredDesigns()
        );
    }

        private String createWorkRequestSystemPrompt(String outputFormat) {
        // DesignCategory enum의 모든 값들을 동적으로 가져오기
        String availableCategories = Arrays.stream(DesignCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining("\", \"", "[\"", "\"]"));
        
        // 현재 날짜 기반으로 유효한 날짜 범위 계산
        LocalDate currentDate = LocalDate.now();
        LocalDate minEndDate = currentDate.plusDays(7);  // 최소 1주일 후
        LocalDate maxEndDate = currentDate.plusMonths(2); // 최대 2개월 후
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String minEndDateStr = minEndDate.format(formatter);
        String maxEndDateStr = maxEndDate.format(formatter);

        // 제안 금액 범위
        String minBudget = "500000";
        String maxBudget = "1000000";
        String exampleBudget = "510000";
        
        return """
                당신은 디자인을 의뢰하는 클라이언트의 관점에서 작업의뢰서를 작성합니다.
                실제 클라이언트가 디자이너에게 친근하고 구체적으로 요청하는 느낌으로 작성해주세요.
                
                각 필드별 작성 가이드라인 및 예시:
                
                1. projectTitle: 구체적이고 매력적인 프로젝트 제목 (최대 30자)
                   예시: "마크업 감성의 빈티지 카페 브랜딩 디자인"
                
                2. endDate: 현실적인 납기일 (YYYY-MM-DD 형식)
                   사용 가능한 날짜 범위: %s ~ %s
                   예시: "%s"
                
                3. budget: 적절한 예산 (원 단위, 예: 510000)
                   사용 가능한 금액 범위: %s ~ %s
                   예시: "%s"
                
                4. productService: 사업체/제품을 친근하게 소개 (최대 100자)
                   예시: "따뜻한 분위기의 빈티지 콘셉트 카페입니다"
                
                5. targetCustomers: 구체적인 타겟 고객 설명 (최대 80자)
                   예시: "20대 여성, 감성적인 SNS 감성을 선호하는 고객층"
                
                6. nowStatus: 현재 진행 상황을 설명 (최대 100자)
                   예시: "매장 오픈 준비 중이며, 브랜드 아이덴티티 구축 단계입니다"
                
                7. goal: 달성하고 싶은 목표를 구체적으로 서술 (최대 150자)
                   예시: "매장 오픈에 맞춰 브랜드의 정체성을 효과적으로 전달할 수 있는 로고 및 시각 자료를 제작하고 싶습니다"
                
                8. designCategories: 필요한 디자인 카테고리 (최대 3개)
                   사용 가능한 값: %s
                
                9. additionalDescription: 참고자료나 원하는 스타일을 구체적으로 설명 (최대 150자)
                   예시: "깔끔하고 따뜻한 느낌의 디자인을 원합니다. 톤다운된 브라운 계열과 자연광이 어우러진 분위기를 참고해주세요"
                
                10. additionalRequirement: 구체적인 요구사항이나 전달 방법 (최대 150자)
                     예시: "최종 시안 확정 후 인쇄용 AI파일 및 웹&SNS 업로드용 PNG 파일을 함께 전달 부탁드립니다"
                
                다음 JSON 형식으로 응답해주세요:
                %s
                
                중요 작성 원칙:
                - 클라이언트가 디자이너에게 친근하게 요청하는 톤으로 작성
                - "~합니다", "~해주세요", "~원합니다", "~부탁드립니다" 등의 정중한 표현 사용
                - 구체적이고 현실적인 내용으로 작성
                - endDate는 반드시 %s ~ %s 범위 내에서만 선택
                - designCategories는 반드시 위 목록의 정확한 값만 사용
                - JSON을 완전하게 닫기
                """.formatted(minEndDateStr, maxEndDateStr, currentDate.plusDays(14).format(formatter),
                minBudget, maxBudget, exampleBudget, availableCategories, outputFormat, minEndDateStr, maxEndDateStr);
    }

    /**
     * AI 응답에서 불완전한 JSON을 정리하고 수정합니다.
     *
     * @param response 원본 AI 응답
     * @return 정리된 JSON 응답
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("AI 응답이 비어있습니다.");
        }

        String cleaned = response.trim();
        
        // JSON 시작과 끝 찾기
        int startIndex = cleaned.indexOf('{');
        if (startIndex == -1) {
            throw new RuntimeException("JSON 형식이 올바르지 않습니다: 시작 괄호 없음");
        }
        
        cleaned = cleaned.substring(startIndex);
        
        // 불완전한 JSON 수정 시도
        if (!cleaned.endsWith("}")) {
            // 마지막 완전한 필드까지만 유지
            int lastCommaIndex = cleaned.lastIndexOf(',');
            if (lastCommaIndex > 0) {
                // 마지막 쉼표 이후의 불완전한 부분 제거
                String beforeLastComma = cleaned.substring(0, lastCommaIndex);
                // JSON 닫기
                cleaned = beforeLastComma + "}";
            } else {
                // 쉼표가 없으면 첫 번째 필드도 불완전할 수 있음
                throw new RuntimeException("JSON 형식이 심각하게 손상되었습니다.");
            }
        }
        
        return cleaned;
    }
}
