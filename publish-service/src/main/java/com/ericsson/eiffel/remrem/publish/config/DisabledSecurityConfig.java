/*
    Copyright 2018 Ericsson AB.
    For a full list of individual contributors, please see the commit history.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.ericsson.eiffel.remrem.publish.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This class is used to disable the ldap authentication based on property
 * activedirectory.publish.enabled = false in property file. By default this
 * class will execute.
 *
 */
@Profile("!integration-test")
@ConditionalOnProperty(value = "activedirectory.publish.enabled", havingValue = "false", matchIfMissing = true)
@Configuration
@EnableWebSecurity
public class DisabledSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
             // The application uses non-browser clients. Yes, there is swagger interface,
             // but is's used only for testing/tuning.
             //
             // From https://docs.spring.io/spring-security/reference/features/exploits/csrf.html
             // "If you are creating a service that is used only by non-browser clients,
             //  you likely want to disable CSRF protection."
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
