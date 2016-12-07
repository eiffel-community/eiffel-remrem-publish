package com.ericsson.eiffel.remrem.publish.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("!integration-test")
@ConditionalOnProperty(prefix = "activedirectory.", value = "enabled", havingValue = "false")
@Configuration
@EnableWebSecurity
public class DisabledSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .anyRequest()
                    .permitAll()
                    .and()
                .csrf()
                    .disable();
    }
}
