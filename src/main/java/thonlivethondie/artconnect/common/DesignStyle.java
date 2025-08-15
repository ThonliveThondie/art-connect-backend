package thonlivethondie.artconnect.common;

import lombok.Getter;

@Getter
public enum DesignStyle {
    MINIMAL("미니멀"),
    MODERN("모던"),
    CLASSIC("클래식"),
    VINTAGE("빈티지"),
    ILLUSTRATION("일러스트"),
    TYPOGRAPHY("타이포그래피");

    private final String description;

    DesignStyle(String description) {
        this.description = description;
    }
}
