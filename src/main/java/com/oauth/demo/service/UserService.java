package com.oauth.demo.service;

import com.oauth.demo.data.UserDto;

public interface UserService {

    UserDto createUser(UserDto userDto);

    void activateUser(String token);

    UserDto getUserByUsername(String username);

    UserDto getUserByUserId(String userId);
}
