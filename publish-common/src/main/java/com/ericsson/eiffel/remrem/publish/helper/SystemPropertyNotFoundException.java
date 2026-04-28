package com.ericsson.eiffel.remrem.publish.helper;

public class SystemPropertyNotFoundException extends Exception {
    public SystemPropertyNotFoundException(String propertyName) {
        super("Java system property not found: '" + propertyName + "'");
    }
}
