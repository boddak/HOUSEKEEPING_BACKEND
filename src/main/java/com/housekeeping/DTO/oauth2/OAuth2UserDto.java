package com.housekeeping.DTO.oauth2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2UserDto {
    private String username;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;

    @Builder
    public OAuth2UserDto(String username, String name, String email, String phoneNumber, String role) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}
