package thonlivethondie.artconnect.common;

public enum UserType {
    SMALL_BUSINESS_OWNER("소상공인"),
    STUDENT_DESIGNER("대학생·신진 디자이너");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
