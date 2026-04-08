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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This class is used to enable the ldap authentication based on property
 * activedirectory.publish.enabled = true in properties file.
 *
 */
@Profile("!integration-test")
@Configuration
@ConditionalOnProperty(value = "activedirectory.publish.enabled")
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${activedirectory.ldapUrl}")
    private String ldapUrl;

    @Value("${jasypt.encryptor.jasyptKeyFilePath:{null}}")
    private String jasyptKeyFilePath;

    @Value("${activedirectory.managerPassword}")
    private String managerPassword;

    @Value("${activedirectory.managerDn}")
    private String managerDn;

    @Value("${activedirectory.userSearchFilter}")
    private String userSearchFilter;

    @Value("${activedirectory.rootDn}")
    private String rootDn;

    @Value("${activedirectory.connectionTimeOut:#{127000}}")
    private Integer ldapTimeOut = DEFAULT_LDAP_CONNECTION_TIMEOUT;

//  built in connection timeout value for ldap if the network issue happens
    public static final Integer DEFAULT_LDAP_CONNECTION_TIMEOUT = 127000;

    public Integer getTimeOut() {
        return ldapTimeOut;
    }

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public AuthenticationProvider ldapAuthenticationProvider() {
        final String jasyptKey = RabbitMqPropertiesConfig.readJasyptKeyFile(jasyptKeyFilePath);
        if (managerPassword.startsWith("{ENC(") && managerPassword.endsWith("}")) {
            managerPassword = DecryptionUtils.decryptString(
                    managerPassword.substring(1, managerPassword.length() - 1), jasyptKey);
        }
        LOGGER.debug("LDAP server url: " + ldapUrl);

        // Initialize and configure the LdapContextSource
        LdapContextSource contextSource = ldapContextSource();

        // Configure BindAuthenticator with the context source and user search filter
        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        bindAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(
                "", // Empty base indicates search starts at root DN provided in contextSource
                userSearchFilter,
                contextSource));

        return new LdapAuthenticationProvider(bindAuthenticator);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(ldapAuthenticationProvider());
        return builder.build();
    }

    public LdapContextSource ldapContextSource() {
        LdapContextSource ldap = new LdapContextSource();
        ldap.setUrl(ldapUrl);
        ldap.setBase(rootDn);
        ldap.setUserDn(managerDn);
        ldap.setPassword(managerPassword);
        HashMap<String, Object> environment = new HashMap<>();
        environment.put("com.sun.jndi.ldap.connect.timeout", Integer.toString(getTimeOut()));
        ldap.setBaseEnvironmentProperties(environment);
        ldap.afterPropertiesSet();
        return ldap;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.debug("LDAP authentication enabled");
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(basic -> basic.authenticationEntryPoint(customAuthenticationEntryPoint))
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
