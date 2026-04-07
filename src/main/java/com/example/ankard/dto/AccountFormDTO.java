package com.example.ankard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountFormDTO {
    private String email;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}
