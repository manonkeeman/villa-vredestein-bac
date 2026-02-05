package com.villavredestein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequestDTO {

    @NotBlank(message = "oldPassword is verplicht")
    private String oldPassword;

    @NotBlank(message = "newPassword is verplicht")
    @Size(min = 8, max = 255, message = "newPassword moet minimaal 8 tekens zijn")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}