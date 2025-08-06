package thonlivethondie.artcorner.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import thonlivethondie.artcorner.jwt.JwtHandler;
import thonlivethondie.artcorner.repository.RefreshTokenRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtHandler jwtHandler;

    /**
     * 매일 새벽 3시에 만료된 리프레시 토큰 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRefreshTokens() {
        log.info("만료된 리프레시 토큰 삭제 작업 시작");

        refreshTokenRepository.findAll().forEach(token -> {
            var claimsOpt = jwtHandler.getClaims(token.getToken());
            if (claimsOpt.isEmpty()) {
                log.info("만료된 토큰 삭제: userId={}", token.getUserId());
                refreshTokenRepository.delete(token);
            }
        });

        log.info("리프레시 토큰 삭제 완료");
    }
}
