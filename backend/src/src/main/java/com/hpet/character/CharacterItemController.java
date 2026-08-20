package com.hpet.character;

import com.hpet.character.dto.CharacterItemResponse;
import com.hpet.character.dto.EquipItemRequest;
import com.hpet.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Character Costume", description = "캐릭터 코스튬 착용/해제 - 어린이 단계(7일차)부터 가능 (Phase 2)")
@RestController
@RequestMapping("/api/character/me/items")
public class CharacterItemController {

    private final CharacterService characterService;

    public CharacterItemController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @Operation(summary = "아이템 착용")
    @PostMapping
    public ResponseEntity<ApiResponse<CharacterItemResponse>> equip(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody EquipItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(characterService.equipItem(userId, request)));
    }

    @Operation(summary = "아이템 해제")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> unequip(
            @AuthenticationPrincipal Long userId, @PathVariable Long itemId) {
        characterService.unequipItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
