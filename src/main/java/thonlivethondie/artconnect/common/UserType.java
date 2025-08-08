package thonlivethondie.artconnect.common;

public enum UserType {
    BUSINESS_OWNER("소상공인"),
    DESIGNER("디자이너");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
