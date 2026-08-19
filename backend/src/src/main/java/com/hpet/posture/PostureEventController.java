package com.hpet.posture;

import com.hpet.common.ApiResponse;
import com.hpet.posture.dto.PostureEventCreateRequest;
import com.hpet.posture.dto.PostureEventResponse;
import com.hpet.posture.dto.PostureSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Posture", description = "자세 교정 (Phase 4)")
@RestController
@RequestMapping("/api/posture-events")
public class PostureEventController {

    private final PostureEventService postureEventService;

    public PostureEventController(PostureEventService postureEventService) {
        this.postureEventService = postureEventService;
    }

    @Operation(summary = "자세 불량 이벤트 저장 - 판정은 클라이언트에서 완료된 상태로 전달됨")
    @PostMapping
    public ResponseEntity<ApiResponse<PostureEventResponse>> register(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody PostureEventCreateRequest request) {
        PostureEventResponse response = postureEventService.register(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "자세 불량 이력 조회 - startDate/endDate 없으면 최근 7일 기본 적용")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostureEventResponse>>> getHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(postureEventService.getHistory(userId, startDate, endDate)));
    }

    @Operation(summary = "일자별 자세 불량 발생 횟수 집계 - startDate/endDate 없으면 최근 7일 기본 적용")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<PostureSummaryResponse>>> getSummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(postureEventService.getSummary(userId, startDate, endDate)));
    }
}
