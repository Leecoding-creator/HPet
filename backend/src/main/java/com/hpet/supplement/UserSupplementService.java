package com.hpet.supplement;

import com.hpet.character.CharacterAssignmentService;
import com.hpet.common.exception.SupplementNotFoundException;
import com.hpet.domain.supplement.Supplement;
import com.hpet.domain.supplement.SupplementRepository;
import com.hpet.domain.supplement.UserSupplement;
import com.hpet.domain.supplement.UserSupplementRepository;
import com.hpet.supplement.dto.RegisterSupplementsRequest;
import com.hpet.supplement.dto.UserSupplementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Phase 2 - 2-3. 사용자 영양제 등록.
 * 등록 직후, 아직 캐릭터가 없는 사용자라면 CharacterAssignmentService로 배정을 시도한다
 * (회의 1-2 확정 로직: 매핑되는 영양제가 있어야 배정됨).
 */
@Service
public class UserSupplementService {

    private static final Logger log = LoggerFactory.getLogger(UserSupplementService.class);

    private final SupplementRepository supplementRepository;
    private final UserSupplementRepository userSupplementRepository;
    private final CharacterAssignmentService characterAssignmentService;

    public UserSupplementService(SupplementRepository supplementRepository,
                                  UserSupplementRepository userSupplementRepository,
                                  CharacterAssignmentService characterAssignmentService) {
        this.supplementRepository = supplementRepository;
        this.userSupplementRepository = userSupplementRepository;
        this.characterAssignmentService = characterAssignmentService;
    }

    @Transactional
    public List<UserSupplementResponse> register(Long userId, RegisterSupplementsRequest request) {
        for (Long supplementId : request.getSupplementIds()) {
            Supplement supplement = supplementRepository.findById(supplementId)
                    .orElseThrow(() -> new SupplementNotFoundException(supplementId));

            boolean alreadyRegistered = userSupplementRepository.existsByUserIdAndSupplement(userId, supplement);
            if (!alreadyRegistered) {
                userSupplementRepository.save(new UserSupplement(userId, supplement));
                log.info("Supplement registered: userId={}, supplement={}", userId, supplement.getName());
            }
        }

        // 지금까지 등록된 전체 영양제 이름으로 캐릭터 배정을 시도 (이미 배정돼 있으면 내부에서 스킵됨)
        List<String> allRegisteredNames = userSupplementRepository.findByUserId(userId).stream()
                .map(us -> us.getSupplement().getName())
                .toList();
        characterAssignmentService.assignIfNeeded(userId, allRegisteredNames);

        return getMyList(userId);
    }

    @Transactional(readOnly = true)
    public List<UserSupplementResponse> getMyList(Long userId) {
        return userSupplementRepository.findByUserId(userId).stream()
                .map(us -> new UserSupplementResponse(
                        us.getId(), us.getSupplement().getId(), us.getSupplement().getName(), us.getRegisteredAt()))
                .toList();
    }
}
