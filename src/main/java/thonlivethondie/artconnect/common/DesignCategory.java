package thonlivethondie.artconnect.common;

import lombok.Getter;

@Getter
public enum DesignCategory {
    LOGO("로고 디자인"),
    BRAND("브랜드 디자인"),
    GOODS("굿즈 디자인"),
    POSTER_FLYER("포스터/전단지 디자인"),
    BANNER_AD("배너/광고 디자인"),
    PACKAGE("패키지 디자인"),
    CARD("명함/카드/인쇄물 디자인");

    private final String description;

    DesignCategory(String description) {
        this.description = description;
    }
}
