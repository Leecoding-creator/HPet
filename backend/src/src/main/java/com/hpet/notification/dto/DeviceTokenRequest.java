package com.hpet.notification.dto;

import com.hpet.domain.notification.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeviceTokenRequest {

    @NotBlank(message = "token은 필수입니다.")
    private String token;

    @NotNull(message = "platform은 필수입니다.")
    private DevicePlatform platform;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public DevicePlatform getPlatform() { return platform; }
    public void setPlatform(DevicePlatform platform) { this.platform = platform; }
}
