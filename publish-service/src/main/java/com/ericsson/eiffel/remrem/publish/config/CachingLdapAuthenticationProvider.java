package com.ericsson.eiffel.remrem.publish.config;

import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;

public class CachingLdapAuthenticationProvider extends LdapAuthenticationProvider {
	
	private UserCache userCache = new NullUserCache();
	public CachingLdapAuthenticationProvider(LdapAuthenticator authenticator) {
		super(authenticator);
		// TODO Auto-generated constructor stub
	}
	public UserCache getUserCache() {
		return userCache;
	}
	public void setUserCache(UserCache userCache) {
		this.userCache = userCache;
	}	
}
