package com.oauth.demo.mapper;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import org.springframework.stereotype.Component;

@Component
public class BaseMapper extends ConfigurableMapper {

    @Override
    protected void configureFactoryBuilder(DefaultMapperFactory.Builder factoryBuilder) {
        super.configureFactoryBuilder(factoryBuilder);
        factoryBuilder.compilerStrategy(new EclipseJdtCompilerStrategy());

        // Write out source files to (classpath:)/ma/glasnost/orika/generated/
        System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, Boolean.TRUE.toString());
        // Write out class files to (classpath:)/ma/glasnost/orika/generated/
        System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, Boolean.TRUE.toString());
    }

}
