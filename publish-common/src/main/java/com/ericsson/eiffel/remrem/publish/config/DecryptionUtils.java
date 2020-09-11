/*
    Copyright 2019 Ericsson AB.
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

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 *
 * This Utility class is used for decryption of any encrypted property.
 *
 */
public class DecryptionUtils {

    private DecryptionUtils() {
        // Avoid Object creation, contains all static methods.
    }

    /**
     * Used to decrypt a string using StandardPBEStringEncryptor.<br>
     * Encryption String needs to be in the format of ENC(encryptedValue).
     *
     * @param encryptedValue
     *            - Any encrypted property which need to be decrypted.
     * @param decryptionKey
     *            - secretkey which was used at time of encrypting open text property to be passed to decrypt the same.
     * @return decrypted value if input string matches the ENC() format matches, else original value passed.
     */
    protected static String decryptString(final String encryptedValue, final String decryptionKey) {
        if (encryptedValue == null || encryptedValue.isEmpty()) {
            return "";
        }
        if (decryptionKey == null || decryptionKey.isEmpty()) {
            return encryptedValue;
        }
        if (encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
            final String encryptedPassword = StringUtils.substringBetween(encryptedValue, "ENC(", ")");
            final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(decryptionKey);
            return encryptor.decrypt(encryptedPassword);
        } else {
            return encryptedValue;
        }
    }
}
