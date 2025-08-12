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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final AnthropicChatModel anthropicChatModel;

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
}
