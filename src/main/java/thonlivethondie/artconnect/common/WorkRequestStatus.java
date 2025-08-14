package thonlivethondie.artconnect.common;

import lombok.Getter;

@Getter
public enum WorkRequestStatus {
    PENDING("시안 제출 대기 중"),
    FEEDBACK_WAITING("피드백 대기 중"),
    ACCEPTED("피드백 전달 완료");

    private final String description;

    WorkRequestStatus(String description) {
        this.description = description;
    }
}
