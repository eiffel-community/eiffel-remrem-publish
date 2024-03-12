package com.ericsson.eiffel.remrem.publish.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;

@Service
public class CustomLdapUserDetailsService implements UserDetailsService{
    

    @Value("${activedirectory.rootDn}")
    private String rootDn;
    
    @Autowired
    private LdapTemplate ldapTemplate;
    
    @Autowired
    private CacheManager cacheManager;

    private Logger log = (Logger) LoggerFactory.getLogger(CustomLdapUserDetailsService.class);
    
    @Cacheable(value = "ldapUserDetailsCache", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Cache cache = cacheManager.getCache("ldapUserDetailsCache");
        if (cache != null && cache.get(username) != null) {
            return cache.get(username, UserDetails.class);
        } else {
            log.info("---------------------at 41--------------");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username1 = userDetails.getUsername();
            String password = userDetails.getPassword();
            log.info("------------ at 46------------" + username+" "+ password);
            log.info("------------------------- 47----------- "+ userDetails);
            cacheManager.getCache("ldapUserDetailsCache").put(username, userDetails);
            return userDetails;
        }
    }
    
    /*
     * private UserDetails mapToUserDetails(Attributes attributes) { // Extract and map attributes
     * to UserDetails object // Example: String username = null;
     * log.info("--------------- at 65----------------- "); try { username =
     * attributes.get("sAMAccountName").get().toString(); } catch(NamingException e) {
     * 
     * } // System.out.println("------------------------ at 56 ------------- "+ username); // String
     * password = attributes.get("userPassword").get().toString(); // ...
     * 
     * // Create and return UserDetails object // Example: return User.withUsername(username) //
     * .password(password) .roles("USER") .build();
     * 
     * // Implement attribute extraction and UserDetails creation logic based on your LDAP schema //
     * return null; }
     */

    /*
     * @Override public UserDetails loadUserByUsername(String username) throws
     * UsernameNotFoundException { Cache cache = cacheManager.getCache("ldapUserDetailsCache"); if
     * (cache != null && cache.get(username) != null) { return cache.get(username,
     * UserDetails.class); } else { Authentication authentication =
     * SecurityContextHolder.getContext().getAuthentication(); String authenticatedUsername =
     * authentication.getName();
     * 
     * UserDetails userDetails = getUserDetailsByUsername(authenticatedUsername);
     * 
     * cache.put(authenticatedUsername, userDetails);
     * 
     * return userDetails;
     * 
     * // throw new UsernameNotFoundException("User not found"); } } public UserDetails
     * getUserDetailsByUsername(String username) { // Assuming 'cn' is the attribute for the
     * username in LDAP String ldapQuery = "(cn=" + username + ")";
     * 
     * try { return ldapTemplate.search( "", // Base DN for the search ldapQuery, // LDAP filter
     * (AttributesMapper<UserDetails>) attributes -> { // Map LDAP attributes to UserDetails object
     * // Example mapping; adapt based on your LDAP schema String ldapUsername =
     * attributes.get("cn").get().toString(); // String ldapPassword = ""; // Fetch password
     * attribute // Other attribute mappings as needed
     * 
     * return org.springframework.security.core.userdetails.User .withUsername(ldapUsername) //
     * .password(ldapPassword) .roles("USER") // Set user roles based on LDAP attributes // Other
     * attribute settings as needed .build(); }).stream().findFirst().orElse(null); // Fetch the
     * first matching entry } catch (Exception e) { throw new
     * UsernameNotFoundException("User not found");
     */

}
