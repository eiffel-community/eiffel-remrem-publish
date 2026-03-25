/*
    Copyright 2026 Ericsson AB.
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
