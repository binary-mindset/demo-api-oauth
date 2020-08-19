package com.oauth.demo.mapper;

import com.oauth.demo.data.UserDto;
import com.oauth.demo.model.User;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends BaseMapper {

    @Override
    protected void configure(MapperFactory mapperFactory) {
        super.configure(mapperFactory);

        // Api mapping
        mapperFactory.classMap(User.class, UserDto.class)
                .byDefault()
                .register();

        // Keycloak mapping
        mapperFactory.classMap(UserDto.class, UserRepresentation.class)
                .field("userId", "id")
                .field("", "credentials[0]")
                .byDefault()
                .register();

        mapperFactory.classMap(UserDto.class, CredentialRepresentation.class)
                .field("password", "value")
                .customize(new CustomMapper<UserDto, CredentialRepresentation>() {
                    @Override
                    public void mapAtoB(UserDto userDto, CredentialRepresentation credentialRepresentation, MappingContext context) {
                        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                        credentialRepresentation.setTemporary(Boolean.FALSE);
                    }
                })
                .byDefault()
                .register();
    }
}
