package com.oauth.demo.controller;

import com.oauth.demo.AdminApiDelegate;
import com.oauth.demo.model.HelloResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminController implements AdminApiDelegate {

    @Override
    public ResponseEntity<HelloResponse> helloAdmin() {
        HelloResponse response = new HelloResponse();
        response.setResponse("hello admin!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
