package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import thonlivethondie.artconnect.dto.StoreResponseDto;
import thonlivethondie.artconnect.dto.StoreUpdateRequestDto;
import thonlivethondie.artconnect.service.StoreService;

import java.util.Optional;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 매장명 저장/수정
    @PostMapping({
            "/save/name",
            "/save/address",
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
}
