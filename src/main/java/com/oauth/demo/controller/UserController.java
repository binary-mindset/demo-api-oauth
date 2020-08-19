package com.oauth.demo.controller;

import com.oauth.demo.UsersApiDelegate;
import com.oauth.demo.data.UserDto;
import com.oauth.demo.model.User;
import com.oauth.demo.service.MailService;
import com.oauth.demo.service.UserService;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UsersApiDelegate {

    private final UserService userService;
    private final MailService mailService;
    private final MapperFacade mapperFacade;

    @Autowired
    public UserController(final UserService userService, final MailService mailService, final MapperFacade userMapper) {
        this.userService = userService;
        this.mailService = mailService;
        this.mapperFacade = userMapper;
    }

    @Override
    public ResponseEntity<User> createUser(User user) {

        UserDto newUser = userService.createUser(mapperFacade.map(user, UserDto.class));

        // Send mail to activate account
        mailService.sendEmail("Activate your account", newUser.getEmail(), "http://link-to-activate/" + newUser.getActivationToken());

        return new ResponseEntity<>(mapperFacade.map(newUser, User.class), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> activateUser(String token) {
        userService.activateUser(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
