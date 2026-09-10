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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;

/**
 * A wrapper around {@link BindAuthenticator} that retries LDAP bind operations
 * when transient communication failures occur (e.g., connection unexpectedly closed).
 *
 * <p>Only {@link org.springframework.ldap.CommunicationException} triggers a retry.
 * Authentication failures such as bad credentials are not retried.</p>
 */
public class RetryingBindAuthenticator extends BindAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryingBindAuthenticator.class);

    private final BindAuthenticator delegate;
    private final RetryTemplate retryTemplate;

    /**
     * @param contextSource the LDAP context source (passed to the parent constructor)
     * @param delegate      the actual BindAuthenticator that performs the LDAP bind
     * @param retryTemplate the RetryTemplate configured with the desired retry/backoff policy
     */
    public RetryingBindAuthenticator(LdapContextSource contextSource,
                                     BindAuthenticator delegate,
                                     RetryTemplate retryTemplate) {
        super(contextSource);
        this.delegate = delegate;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public DirContextOperations authenticate(Authentication authentication) {
        return retryTemplate.execute(context -> {
            if (context.getRetryCount() > 0) {
                LOGGER.warn("Retrying LDAP bind authentication, attempt {}",
                        context.getRetryCount() + 1);
            }
            return delegate.authenticate(authentication);
        });
    }
}
