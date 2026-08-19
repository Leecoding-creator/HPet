package com.hpet.domain.agreement;

/**
 * 동의가 필요한 약관/정보 수집 항목.
 * HEALTH_INFO는 건강 프로필(영양제 복용 등 민감정보)을 다루는 앱 특성상 별도로 분리했다.
 */
public enum AgreementType {
    TERMS_OF_SERVICE,       // 서비스 이용약관
    PRIVACY_POLICY,         // 개인정보처리방침
    HEALTH_INFO_COLLECTION  // 건강정보 수집·이용 동의
}
