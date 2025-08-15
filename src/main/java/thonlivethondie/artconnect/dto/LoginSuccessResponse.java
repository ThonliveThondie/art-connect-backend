package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.UserType;

public record LoginSuccessResponse(
        Long userId,
        UserType userType,
        String message
) {
}
