package thonlivethondie.artconnect.common;

import lombok.Getter;

@Getter
public enum UserType {
    BUSINESS_OWNER("소상공인"),
    DESIGNER("디자이너");

    private final String description;

    UserType(String description) {
        this.description = description;
    }
}
