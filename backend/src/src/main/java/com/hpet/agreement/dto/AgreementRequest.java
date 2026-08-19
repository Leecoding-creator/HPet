package com.hpet.agreement.dto;

import com.hpet.domain.agreement.AgreementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AgreementRequest {

    @NotNull(message = "동의 항목(type)은 필수입니다.")
    private AgreementType type;

    @NotBlank(message = "약관 버전은 필수입니다.")
    private String version;

    private boolean agreed;

    public AgreementType getType() { return type; }
    public void setType(AgreementType type) { this.type = type; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public boolean isAgreed() { return agreed; }
    public void setAgreed(boolean agreed) { this.agreed = agreed; }
}
