package com.hpet.character;

import com.hpet.character.dto.CharacterResponse;
import com.hpet.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Character", description = "내 캐릭터 조회 (Phase 2)")
@RestController
@RequestMapping("/api/character")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Operation(summary = "내 캐릭터 조회 - 배정된 캐릭터, 성장 단계, 착용 아이템 목록")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CharacterResponse>> getMine(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(characterService.getMyCharacter(userId)));
    }
}
