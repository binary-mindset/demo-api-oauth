package com.oauth.demo.service.impl;

import com.oauth.demo.data.UserDto;
import com.oauth.demo.mapper.UserMapper;
import com.oauth.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import javax.ws.rs.core.Response;
import java.util.List;

@Service
@Slf4j
public class KeycloakUserServiceImpl implements UserService {

    private final Keycloak keycloakClient;
    private final MapperFacade mapperFacade;

    private final static String PUBLICARS_REALM = "demo-api";

    @Autowired
    public KeycloakUserServiceImpl(final Keycloak keycloakClient, final UserMapper userMapper) {
        this.keycloakClient = keycloakClient;
        this.mapperFacade = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        UserRepresentation keycloakUser = mapperFacade.map(userDto, UserRepresentation.class);
        keycloakUser.setEmailVerified(false);
        // We will activate the account once the user has confirmed the email
        keycloakUser.setEnabled(false);

        // Create the user in Keycloak
        RealmResource realmResource = keycloakClient.realm(PUBLICARS_REALM);
        Response response = realmResource.users().create(keycloakUser);

        if (201 != response.getStatus()) {
            log.error("Error when creating the user");
            throw Problem.builder()
                    .withStatus(Status.CONFLICT)
                    .withTitle(Status.CONFLICT.getReasonPhrase())
                    .withDetail("Error when creating the user").build();
        }

        // Extract the user id from the keycloak response
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        userDto.setActivationToken(userId);
        return userDto;
    }

    @Override
    public void activateUser(String token) {
        UserRepresentation userRepresentation = getKeycloakUserById(token);
        assert userRepresentation != null;
        userRepresentation.setEnabled(Boolean.TRUE);
        userRepresentation.setEmailVerified(Boolean.TRUE);
        keycloakClient.realm(PUBLICARS_REALM).users().get(token).update(userRepresentation);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        List<UserRepresentation> results = keycloakClient.realm(PUBLICARS_REALM).users().search(username);

        if (results == null || results.isEmpty()) {
            log.error("User not found");
            throw Problem.builder()
                    .withStatus(Status.NOT_FOUND)
                    .withTitle(Status.NOT_FOUND.getReasonPhrase())
                    .withDetail("User not found").build();
        }

        return mapperFacade.map(results.stream().findFirst().get(), UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        return mapperFacade.map(getKeycloakUserById(userId), UserDto.class);
    }


    /**
     * This method returns a user with the given id from Keycloak
     *
     * @param userId the keycloak userId
     * @return the keycloak user
     */
    private UserRepresentation getKeycloakUserById(String userId) {
        UserResource userResource = keycloakClient.realm(PUBLICARS_REALM).users().get(userId);

        if (userId == null) {
            log.error("User not found");
            throw Problem.builder()
                    .withStatus(Status.NOT_FOUND)
                    .withTitle(Status.NOT_FOUND.getReasonPhrase())
                    .withDetail("User not found").build();
        }
        return userResource.toRepresentation();
    }
}
