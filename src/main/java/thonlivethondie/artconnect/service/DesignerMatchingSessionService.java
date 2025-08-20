package thonlivethondie.artconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thonlivethondie.artconnect.dto.DesignerMatchingSessionDto;
import thonlivethondie.artconnect.dto.RecommendedDesignerDto;
import thonlivethondie.artconnect.dto.ScoredDesignerDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 디자이너 매칭 세션을 관리하는 서비스 클래스
 * 메모리 기반으로 매칭 결과를 저장하고 새로고침 기능을 제공합니다.
 */
@Slf4j
@Service
public class DesignerMatchingSessionService {

    /**
     * 세션 저장소 (메모리 기반)
     * 실제 운영 환경에서는 Redis 등의 외부 캐시를 사용하는 것을 권장
     */
    private final Map<String, DesignerMatchingSessionDto> sessionStore = new ConcurrentHashMap<>();

    /**
     * 세션 만료 시간 (30분)
     */
    private static final int SESSION_EXPIRE_MINUTES = 30;

    /**
     * 새로운 세션을 생성하고 저장합니다.
     *
     * @param session 저장할 세션 데이터
     * @return 생성된 세션 ID
     */
    public String createSession(DesignerMatchingSessionDto session) {
        String sessionId = generateSessionId();
        session.setSessionId(sessionId);
        session.setCreatedAt(LocalDateTime.now());
        session.setReturnedDesignerIds(new ArrayList<>());

        sessionStore.put(sessionId, session);
        log.info("새로운 디자이너 매칭 세션 생성 - 세션 ID: {}, 전체 디자이너 수: {}", 
                sessionId, session.getAllMatchingDesigners().size());

        // 만료된 세션 정리 (백그라운드 작업)
        cleanupExpiredSessions();

        return sessionId;
    }

    /**
     * 세션에서 초기 디자이너 2명을 반환합니다 (1등, 2등).
     *
     * @param sessionId 세션 ID
     * @return 초기 추천 디자이너 목록
     */
    public List<RecommendedDesignerDto> getInitialRecommendations(String sessionId) {
        DesignerMatchingSessionDto session = getSession(sessionId);
        if (session == null || session.getAllMatchingDesigners().isEmpty()) {
            return List.of();
        }

        List<ScoredDesignerDto> allDesigners = session.getAllMatchingDesigners();
        
        // 상위 2명 선택 (1등, 2등)
        List<RecommendedDesignerDto> initialRecommendations = allDesigners.stream()
                .limit(2)
                .map(ScoredDesignerDto::getDesigner)
                .collect(Collectors.toList());

        // 반환된 디자이너 ID 기록
        List<Long> returnedIds = initialRecommendations.stream()
                .map(RecommendedDesignerDto::getUserId)
                .collect(Collectors.toList());
        session.getReturnedDesignerIds().addAll(returnedIds);

        log.info("초기 디자이너 추천 반환 - 세션 ID: {}, 디자이너 수: {}", sessionId, initialRecommendations.size());
        return initialRecommendations;
    }

    /**
     * 세션에서 새로고침된 디자이너 2명을 랜덤으로 반환합니다.
     *
     * @param sessionId 세션 ID
     * @return 새로고침된 추천 디자이너 목록
     */
    public List<RecommendedDesignerDto> getRefreshedRecommendations(String sessionId) {
        DesignerMatchingSessionDto session = getSession(sessionId);
        if (session == null || session.getAllMatchingDesigners().isEmpty()) {
            log.warn("세션을 찾을 수 없거나 매칭된 디자이너가 없음 - 세션 ID: {}", sessionId);
            return List.of();
        }

        List<ScoredDesignerDto> allDesigners = session.getAllMatchingDesigners();
        
        // 아직 반환되지 않은 디자이너들을 우선적으로 선택
        List<ScoredDesignerDto> availableDesigners = allDesigners.stream()
                .filter(scored -> !session.getReturnedDesignerIds().contains(scored.getDesigner().getUserId()))
                .collect(Collectors.toList());

        // 사용 가능한 디자이너가 2명 미만이면 전체 목록에서 랜덤 선택
        if (availableDesigners.size() < 2) {
            availableDesigners = new ArrayList<>(allDesigners);
            log.info("사용 가능한 디자이너가 부족하여 전체 목록에서 선택 - 세션 ID: {}", sessionId);
        }

        // 랜덤하게 2명 선택
        Collections.shuffle(availableDesigners);
        List<RecommendedDesignerDto> refreshedRecommendations = availableDesigners.stream()
                .limit(2)
                .map(ScoredDesignerDto::getDesigner)
                .collect(Collectors.toList());

        // 반환된 디자이너 ID 기록 (중복 제거)
        List<Long> newReturnedIds = refreshedRecommendations.stream()
                .map(RecommendedDesignerDto::getUserId)
                .filter(id -> !session.getReturnedDesignerIds().contains(id))
                .collect(Collectors.toList());
        session.getReturnedDesignerIds().addAll(newReturnedIds);

        log.info("새로고침 디자이너 추천 반환 - 세션 ID: {}, 디자이너 수: {}", sessionId, refreshedRecommendations.size());
        return refreshedRecommendations;
    }

    /**
     * 세션 ID로 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 세션 데이터 (없으면 null)
     */
    public DesignerMatchingSessionDto getSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        DesignerMatchingSessionDto session = sessionStore.get(sessionId);
        
        // 세션 만료 확인
        if (session != null && isSessionExpired(session)) {
            sessionStore.remove(sessionId);
            log.info("만료된 세션 제거 - 세션 ID: {}", sessionId);
            return null;
        }

        return session;
    }

    /**
     * 고유한 세션 ID를 생성합니다.
     *
     * @return 생성된 세션 ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 세션이 만료되었는지 확인합니다.
     *
     * @param session 확인할 세션
     * @return 만료된 경우 true
     */
    private boolean isSessionExpired(DesignerMatchingSessionDto session) {
        LocalDateTime expireTime = session.getCreatedAt().plusMinutes(SESSION_EXPIRE_MINUTES);
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 만료된 세션들을 정리합니다.
     */
    private void cleanupExpiredSessions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<String> expiredSessionIds = sessionStore.entrySet().stream()
                    .filter(entry -> {
                        LocalDateTime expireTime = entry.getValue().getCreatedAt().plusMinutes(SESSION_EXPIRE_MINUTES);
                        return now.isAfter(expireTime);
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            expiredSessionIds.forEach(sessionStore::remove);
            
            if (!expiredSessionIds.isEmpty()) {
                log.info("만료된 세션 {} 개 정리 완료", expiredSessionIds.size());
            }
        } catch (Exception e) {
            log.warn("세션 정리 중 오류 발생", e);
        }
    }

    /**
     * 현재 저장된 세션 수를 반환합니다 (모니터링용).
     *
     * @return 현재 세션 수
     */
    public int getActiveSessionCount() {
        cleanupExpiredSessions();
        return sessionStore.size();
    }
}
