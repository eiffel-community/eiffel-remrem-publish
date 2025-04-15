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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

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

    @Value("${LdapCacheTTL}")
    private Integer LdapCacheTTL;

//  built in connection timeout value for ldap if the network issue happens
    public static final Integer DEFAULT_LDAP_CONNECTION_TIMEOUT = 127000;

    public Integer getTimeOut() {
        return ldapTimeOut;
    }

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public UserCache userCache() {
        if (cacheManager().getCache("authenticationCache") == null) {
            throw new IllegalStateException ("Cache 'authenticationCache' is required but not available");
        }
        return new SpringCacheBasedUserCache(cacheManager().getCache("authenticationCache"));
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(LdapCacheTTL, TimeUnit.MINUTES));
        return cacheManager;
    }

    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        LdapContextSource contextSource = ldapContextSource();
        return new DefaultLdapAuthoritiesPopulator(contextSource, null);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        final String jasyptKey = RabbitMqPropertiesConfig.readJasyptKeyFile(jasyptKeyFilePath);
        if (managerPassword.startsWith("{ENC(") && managerPassword.endsWith("}")) {
            managerPassword = DecryptionUtils.decryptString(
                    managerPassword.substring(1, managerPassword.length() - 1), jasyptKey);
        }
        LOGGER.debug("LDAP server url: " + ldapUrl);
        LdapContextSource contextSource = ldapContextSource();
        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        bindAuthenticator.setUserSearch(new FilterBasedLdapUserSearch("", userSearchFilter, contextSource));


        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = ldapAuthoritiesPopulator();

        // Create and use the caching LDAP authentication provider
        CachingLdapAuthenticationProvider cachingProvider =
                new CachingLdapAuthenticationProvider(bindAuthenticator, ldapAuthoritiesPopulator);

        cachingProvider.setUserCache(userCache());
        auth.authenticationProvider(cachingProvider);

    }

    @Bean
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        LOGGER.debug("LDAP authentication enabled");
        http.authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(customAuthenticationEntryPoint)
            .and()
            .csrf()
            .disable();
    }
}
