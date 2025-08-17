package thonlivethondie.artconnect.common;

import lombok.Getter;

@Getter
public enum DesignStyle {
    SIMPLE("심플한"),
    WARM("따뜻한"),
    FANCY("화려한"),
    NEAT("산뜻한"),
    TRANQUIL("차분한"),
    VINTAGE("빈티지"),
    RETRO("레트로"),
    CUTE("귀여운"),
    LOVELY("러블리"),
    REFRESHING("청량한"),
    NATURAL("자연스러운"),
    LUXURIOUS("고급스러운"),
    MODERN("현대적인"),
    CLASSIC("클래식한"),
    EMOTIONAL("감성적인");

    private final String description;

    DesignStyle(String description) {
        this.description = description;
    }
}
