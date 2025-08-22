package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import thonlivethondie.artconnect.dto.StoreNameResponseDto;
import thonlivethondie.artconnect.dto.StoreResponseDto;
import thonlivethondie.artconnect.dto.StoreUpdateRequestDto;
import thonlivethondie.artconnect.service.StoreService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 매장 정보 저장/수정
    @PostMapping({
            "/save/name",
            "/save/type",
            "/save/phone-number",
            "/save/operating-hours"
    })
    public ResponseEntity<StoreResponseDto> saveOrUpdateStore(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid StoreUpdateRequestDto request) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        StoreResponseDto response = storeService.createOrUpdateStore(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 매장 조회
    @GetMapping("/my")
    public ResponseEntity<StoreResponseDto> getMyStore(@AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        Optional<StoreResponseDto> storeOptional = storeService.getMyStore(userId);

        if (storeOptional.isPresent()) {
            return ResponseEntity.ok(storeOptional.get());
        } else {
            return ResponseEntity.noContent().build();  // 204 No Content - 매장이 없는 경우
        }
    }

    /**
     * 매장 이미지 업로드
     */
    @PostMapping("/images")
    public ResponseEntity<StoreResponseDto> uploadStoreImages(
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("매장 이미지 업로드 요청 - userId: {}, 이미지 개수: {}", userId, images.size());

        StoreResponseDto response = storeService.uploadStoreImages(userId, images);

        return ResponseEntity.ok(response);
    }

    /**
     * 매장 이미지 삭제
     */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<StoreResponseDto> deleteStoreImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("매장 이미지 삭제 요청 - userId: {}, imageId: {}", userId, imageId);

        StoreResponseDto response = storeService.deleteStoreImage(userId, imageId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/store-name")
    public ResponseEntity<StoreNameResponseDto> getStoreNameForBusinessOwner(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());

        StoreNameResponseDto response = storeService.getMyStoreName(userId);

        return ResponseEntity.ok(response);
    }
}
