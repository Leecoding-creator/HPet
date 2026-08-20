package com.hpet.supplement;

import com.hpet.domain.supplement.Supplement;
import com.hpet.domain.supplement.SupplementRepository;
import com.hpet.supplement.dto.SupplementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Phase 2 - 2-2. 영양제 마스터데이터 검색.
 */
@Service
public class SupplementService {

    private final SupplementRepository supplementRepository;

    public SupplementService(SupplementRepository supplementRepository) {
        this.supplementRepository = supplementRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplementResponse> search(String keyword) {
        List<Supplement> supplements = (keyword == null || keyword.isBlank())
                ? supplementRepository.findAll()
                : supplementRepository.findByNameContaining(keyword);

        return supplements.stream()
                .map(s -> new SupplementResponse(s.getId(), s.getName()))
                .toList();
    }
}
