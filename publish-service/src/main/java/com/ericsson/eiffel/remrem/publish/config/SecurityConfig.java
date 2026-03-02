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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
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
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private SSLContextReloader contextReloader;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
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
                "",  // Empty base indicates search starts at root DN provided in contextSource
                userSearchFilter,
                contextSource));

        // Setup LdapAuthenticationProvider
        LdapAuthenticationProvider ldapAuthProvider = new LdapAuthenticationProvider(bindAuthenticator);

        // Configure the authentication provider
        auth.authenticationProvider(ldapAuthProvider);
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

    /**
     * Configures HTTP security settings for LDAP authentication.
     * Sets up basic authentication with custom authentication entry point,
     * disables CSRF protection, and registers SSL context reload listeners
     * for handling certificate updates.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        LOGGER.debug("ldap authentication enabled");
        http.authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(customAuthenticationEntryPoint)
            .and()
            .csrf()
            .disable();

        contextReloader.addListener(new SSLContextReloadListener() {
            private DefaultListableBeanFactory beanFactory;

            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> realms;

            /**
             * Called before SSL context reload. Stops Tomcat realms and removes
             * the existing LDAP context source bean to prepare for reload.
             */
            @Override
            public void onContextWillReload() {
                ConfigurableApplicationContext context =
                    (ConfigurableApplicationContext)SecurityConfig.this.getApplicationContext();
                // get bean factory
                beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
                // remove old bean
                beanFactory.destroySingleton("ldapContextSource");

                try {
                    // find Realm MBeans
                    ObjectName query = new ObjectName("Catalina:type=Realm,*");
                    realms = mbs.queryNames(query, null);

                    for (ObjectName realmName : realms) {
                        mbs.invoke(realmName, "stop", null, null);
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while stopping realm: {}", e.getMessage(), e);
                }
            }

            /**
             * Called after SSL context reload. Creates a new LDAP context source
             * with updated SSL settings and restarts the Tomcat realms.
             *
             * @param sslContext the new SSL context after reload
             */
            @Override
            public void onContextReloaded(SSLContext sslContext) {
                // reload ldapContext;
                LdapContextSource contextSource = ldapContextSource();
                // register new bean
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
    }

    // // This switches authentication off. Useful for some testing.
    // @Override
    // protected void configure(HttpSecurity http) throws Exception {
    //     http.authorizeRequests().anyRequest().permitAll().and().csrf().disable();
    // }
}
