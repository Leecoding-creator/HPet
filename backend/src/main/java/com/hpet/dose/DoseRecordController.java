package com.hpet.dose;

import com.hpet.common.ApiResponse;
import com.hpet.dose.dto.DoseRecordCreateRequest;
import com.hpet.dose.dto.DoseRecordResponse;
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

@Tag(name = "Dose Record", description = "복용 기록 (Phase 4)")
@RestController
@RequestMapping("/api/dose-records")
public class DoseRecordController {

    private final DoseRecordService doseRecordService;

    public DoseRecordController(DoseRecordService doseRecordService) {
        this.doseRecordService = doseRecordService;
    }

    @Operation(summary = "복용 수동 체크 - 등록 즉시 인증 완료(verified=true) 처리됨")
    @PostMapping
    public ResponseEntity<ApiResponse<DoseRecordResponse>> register(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody DoseRecordCreateRequest request) {
        DoseRecordResponse response = doseRecordService.registerManual(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "날짜별 복용 기록 조회 - date 파라미터 없으면 오늘 날짜 기준")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoseRecordResponse>>> getByDate(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DoseRecordResponse> response = doseRecordService.getByDate(userId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
