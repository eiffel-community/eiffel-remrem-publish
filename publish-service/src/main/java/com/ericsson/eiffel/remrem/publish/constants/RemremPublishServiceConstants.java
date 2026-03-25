package com.ericsson.eiffel.remrem.publish.constants;

public final class RemremPublishServiceConstants {
    public static final String JSON_STATUS_RESULT = "result";

    public static final String JSON_EVENT_MESSAGE_FIELD = "status message";

    public static final String JSON_STATUS_CODE = "status code";

    public enum ResultStatus {
        FAIL,
        FATAL
    }
}
