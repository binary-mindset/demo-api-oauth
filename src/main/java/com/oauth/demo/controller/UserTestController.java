package com.oauth.demo.controller;

import com.oauth.demo.UserApiDelegate;
import com.oauth.demo.model.HelloResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class UserTestController implements UserApiDelegate {

    @Override
    public ResponseEntity<HelloResponse> helloUser() {
        HelloResponse response = new HelloResponse();
        response.setResponse("hello user!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
