package com.hpet.supplement;

import com.hpet.common.ApiResponse;
import com.hpet.supplement.dto.SupplementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Supplement", description = "영양제 마스터데이터 검색 (Phase 2)")
@RestController
@RequestMapping("/api/supplements")
public class SupplementController {

    private final SupplementService supplementService;

    public SupplementController(SupplementService supplementService) {
        this.supplementService = supplementService;
    }

    @Operation(summary = "영양제 검색 (keyword 없으면 전체 목록)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplementResponse>>> search(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(supplementService.search(keyword)));
    }
}
