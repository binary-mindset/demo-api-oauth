package com.oauth.demo.data;

import lombok.Data;

@Data
public class UserDto {

    private String userId;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    private String activationToken;
}
