package thonlivethondie.artconnect.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileImageUpdateRequest {
    private MultipartFile profileImage;
}