package com.ericsson.eiffel.remrem.publish.config;

import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

public class CachingLdapAuthenticationProvider extends LdapAuthenticationProvider {

    private UserCache userCache = new NullUserCache();

    /**
     * Create an instance with the supplied authenticator and authorities populator
     * implementations.
     *
     * @param authenticator        the authentication strategy (bind, password comparison, etc)
     *                             to be used by this provider for authenticating users.
     * @param authoritiesPopulator the strategy for obtaining the authorities for a given
     */

    public CachingLdapAuthenticationProvider(LdapAuthenticator authenticator, LdapAuthoritiesPopulator authoritiesPopulator) {
        super(authenticator, authoritiesPopulator);
    }

    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }


    @Override
    public Authentication authenticate(Authentication authentication)  {
        String userName = authentication.getName();
        UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;
        UserDetails userDetailsFromCache = userCache.getUserFromCache(userName);
        if (userDetailsFromCache != null) {
            additionalAuthenticationChecks(userDetailsFromCache, userToken);
            return createSuccessfulAuthentication(userToken, userDetailsFromCache);
        } else {
            Authentication authenticationFromProvider = super.authenticate(authentication);
            userCache.putUserInCache((UserDetails)authenticationFromProvider.getPrincipal());
            return authenticationFromProvider;
        }

    }

    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        Object credentials = authentication.getCredentials();
        if (!StringUtils.isEmpty(credentials)) {
            String presentedPassword = authentication.getCredentials().toString();
            if (userDetails.getPassword().equals(presentedPassword)) {
                // password matches
                return;
            }
        }
        throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials",
                "Bad credentials"));
    }
}