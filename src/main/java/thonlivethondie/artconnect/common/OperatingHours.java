package thonlivethondie.artconnect.common;

import lombok.Getter;

@Getter
public enum OperatingHours {
    WEEKDAYS("평일"),
    WEEKENDS("주말/공휴일"),
    MORNING("오전"),
    AFTERNOON("오후"),
    EVENING("저녁"),
    LATE_NIGHT("심야");

    private final String description;

    OperatingHours(String description) {
        this.description = description;
    }
}
