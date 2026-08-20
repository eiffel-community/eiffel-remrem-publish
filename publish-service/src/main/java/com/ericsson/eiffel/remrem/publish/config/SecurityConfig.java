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

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Set;

import com.ericsson.eiffel.remrem.publish.helper.SSLContextReloadListener;
import com.ericsson.eiffel.remrem.publish.helper.SSLContextReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.ldap.pool.validation.DefaultDirContextValidator;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.web.SecurityFilterChain;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.net.ssl.SSLContext;

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

    public static final Integer DEFAULT_LDAP_CONNECTION_TIMEOUT = 127000;

    public Integer getTimeOut() {
        return ldapTimeOut;
    }

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private SSLContextReloader contextReloader;

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider() throws Exception {
        final String jasyptKey = RabbitMqPropertiesConfig.readJasyptKeyFile(jasyptKeyFilePath);
        String decryptedPassword = managerPassword;
        if (managerPassword.startsWith("{ENC(") && managerPassword.endsWith("}")) {
            decryptedPassword = DecryptionUtils.decryptString(
                    managerPassword.substring(1, managerPassword.length() - 1), jasyptKey);
        }
        LOGGER.debug("LDAP server url: " + ldapUrl);

        LdapContextSource contextSource = ldapContextSource(decryptedPassword);
        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        bindAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(
                "", 
                userSearchFilter,
                contextSource));

        return new LdapAuthenticationProvider(bindAuthenticator);
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

    private LdapContextSource ldapContextSource(String password) {
        LdapContextSource ldap = new LdapContextSource();
        ldap.setUrl(ldapUrl);
        ldap.setBase(rootDn);
        ldap.setUserDn(managerDn);
        ldap.setPassword(password);
        HashMap<String, Object> environment = new HashMap<>();
        environment.put("com.sun.jndi.ldap.connect.timeout", Integer.toString(getTimeOut()));
        ldap.setBaseEnvironmentProperties(environment);
        ldap.afterPropertiesSet();
        return ldap;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.debug("ldap authentication enabled");
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests.anyRequest().authenticated()
            )
            .httpBasic(httpBasic ->
                httpBasic.authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .csrf(csrf -> csrf.disable());

        contextReloader.addListener(new SSLContextReloadListener() {
            private DefaultListableBeanFactory beanFactory;

            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> realms;

            @Override
            public void onContextWillReload() {
                beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
                beanFactory.destroySingleton("ldapContextSource");

                try {
                    ObjectName query = new ObjectName("Catalina:type=Realm,*");
                    realms = mbs.queryNames(query, null);

                    for (ObjectName realmName : realms) {
                        mbs.invoke(realmName, "stop", null, null);
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while stopping realm: {}", e.getMessage(), e);
                }
            }

            @Override
            public void onContextReloaded(SSLContext sslContext) {
                LdapContextSource contextSource = ldapContextSource();
                beanFactory.registerSingleton("ldapContextSource", contextSource);

                try {
                    for (ObjectName realmName : realms) {
                        mbs.invoke(realmName, "start", null, null);
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while starting realm: {}", e.getMessage(), e);
                }
            }
        });

        return http.build();
    }
}
