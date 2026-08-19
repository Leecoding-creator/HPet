package com.hpet.agreement;

import com.hpet.agreement.dto.AgreementRequest;
import com.hpet.agreement.dto.AgreementResponse;
import com.hpet.common.exception.MissingRequiredAgreementException;
import com.hpet.domain.agreement.AgreementType;
import com.hpet.domain.agreement.UserAgreement;
import com.hpet.domain.agreement.UserAgreementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Phase 1 - 1-9. 약관/건강정보 동의 이력 저장.
 * 서비스 이용약관 / 개인정보처리방침 / 건강정보 수집 동의, 이 3가지는 필수 항목으로 취급한다.
 */
@Service
public class AgreementService {

    private static final Logger log = LoggerFactory.getLogger(AgreementService.class);

    private static final Set<AgreementType> REQUIRED_TYPES = Set.of(
            AgreementType.TERMS_OF_SERVICE,
            AgreementType.PRIVACY_POLICY,
            AgreementType.HEALTH_INFO_COLLECTION
    );

    private final UserAgreementRepository userAgreementRepository;

    public AgreementService(UserAgreementRepository userAgreementRepository) {
        this.userAgreementRepository = userAgreementRepository;
    }

    /**
     * 동의 이력을 저장한다. 필수 항목(REQUIRED_TYPES)은 반드시 agreed=true로 제출돼야 하며,
     * 그렇지 않으면 예외를 던진다. 선택 항목은 agreed=false로 제출해도 이력만 남긴다.
     */
    @Transactional
    public List<AgreementResponse> submit(Long userId, List<AgreementRequest> requests) {
        Set<AgreementType> agreedRequiredTypes = requests.stream()
                .filter(AgreementRequest::isAgreed)
                .map(AgreementRequest::getType)
                .collect(java.util.stream.Collectors.toSet());

        for (AgreementType required : REQUIRED_TYPES) {
            if (!agreedRequiredTypes.contains(required)) {
                throw new MissingRequiredAgreementException(required.name());
            }
        }

        for (AgreementRequest request : requests) {
            userAgreementRepository.save(
                    new UserAgreement(userId, request.getType(), request.getVersion(), request.isAgreed()));
        }
        log.info("Agreements recorded for userId={}, count={}", userId, requests.size());

        return getHistory(userId);
    }

    @Transactional(readOnly = true)
    public List<AgreementResponse> getHistory(Long userId) {
        return userAgreementRepository.findByUserIdOrderByAgreedAtDesc(userId).stream()
                .map(a -> new AgreementResponse(a.getType(), a.getVersion(), a.isAgreed(), a.getAgreedAt()))
                .toList();
    }
}
