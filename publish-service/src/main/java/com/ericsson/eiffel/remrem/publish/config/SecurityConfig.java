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
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This class is used to enable the ldap authentication based on property
 * activedirectory.publish.enabled = true in properties file.
 *
 */
@Profile("!integration-test")
@Configuration
@ConditionalOnProperty(value = "activedirectory.publish.enabled")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        final String jasyptKey = RabbitMqPropertiesConfig.readJasyptKeyFile(jasyptKeyFilePath);
        if (managerPassword.startsWith("{ENC(") && managerPassword.endsWith("}")) {
            managerPassword = DecryptionUtils.decryptString(managerPassword.substring(1, managerPassword.length() - 1), jasyptKey);
        }
        LOGGER.debug("LDAP server url: " + ldapUrl);
        auth.ldapAuthentication().userSearchFilter(userSearchFilter).contextSource(ldapContextSource());
    }

    @Bean
    public BaseLdapPathContextSource ldapContextSource() {
        LdapContextSource ldap = new LdapContextSource();
        ldap.setUrl(ldapUrl);
        ldap.setBase(rootDn);
        ldap.setUserDn(managerDn);
        ldap.setPassword(managerPassword);
        HashMap<String, Object> environment = new HashMap<>();
        environment.put("com.sun.jndi.ldap.connect.timeout", Integer.toString(getTimeOut()));
        ldap.setBaseEnvironmentProperties(environment);
        return ldap;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        LOGGER.debug("LDAP authentication enabled");
        http.authorizeRequests().anyRequest().authenticated().and().httpBasic().and().csrf().disable();
    }
}
