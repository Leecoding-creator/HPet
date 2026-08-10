package com.hpet.home;

import com.hpet.common.ApiResponse;
import com.hpet.home.dto.HomeSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 대시보드 (Phase 3)")
@RestController
@RequestMapping("/api/home")
public class HomeSummaryController {

    private final HomeSummaryService homeSummaryService;

    public HomeSummaryController(HomeSummaryService homeSummaryService) {
        this.homeSummaryService = homeSummaryService;
    }

    @Operation(summary = "홈 대시보드 요약 - 오늘 복용 현황 + 캐릭터 상태 + 최근 7일 자세 교정 현황")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<HomeSummaryResponse>> getSummary(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(homeSummaryService.getSummary(userId)));
    }
}
