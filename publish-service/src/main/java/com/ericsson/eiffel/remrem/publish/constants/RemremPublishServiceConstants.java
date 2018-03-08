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
package com.ericsson.eiffel.remrem.publish.constants;

public class RemremPublishServiceConstants {

        public static final String GENERATE_NO_SERVICE_ERROR = "{\"status_code\": 503, \"result\": \"FAIL\", "
                + "\"message\": \"Message protocol is invalid\"}";

        public static final String GENERATE_BAD_REQUEST = "{\"status_code\": 400, \"result\": \"FAIL\", "
                + "\"message\": \"Malformed JSON or incorrect type of event\"}";

        public static final String GENERATE_UNAUTHORIZED = "{\"status_code\": 401, \"result\": \"FAIL\", "
                + "\"message\": \"Unauthorized. Please, check if LDAP for REMReM Generate Service is disabled\"}";

        public static final String GENERATE_INTERNAL_ERROR = "{\"status_code\": 500, \"result\": \"FAIL\", "
                + "\"message\": \"Internal server error\", \"reason\": \"HERE SHOULD BE REASON\"}";

        public static final String DOCUMENTATION_URL = "http://ericsson.github.io/eiffel-remrem-publish/index.html";

}
