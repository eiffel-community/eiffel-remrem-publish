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

public final class RemRemPublishResponseExamples {

    // Response examples for /producer/msg API endpoint

    public static final String PRODUCER_RESPONSE_200_EXAMPLE = """
        {
          "events": [
            {
              "id": "6711ccd3-6835-4e83-8ae6-1b64f249fc52",
              "status_code": 200,
              "result": "SUCCESS",
              "message": "Event sent successfully"
            },
            {
              "id": "a711ccd3-6835-4e83-8ae6-1b64f249fc53",
              "status_code": 200,
              "result": "SUCCESS",
              "message": "Event sent successfully"
            }
          ]
        }
    """;

    public static final String PRODUCER_RESPONSE_400_VALIDATION_EXAMPLE = """
        {
          "events": [
            {
              "status_code": 400,
              "result": "Bad Request",
              "message": "Invalid event content, client need to fix problem in event before submitting again"
            }
          ]
        }
    """;

    public static final String PRODUCER_RESPONSE_400_INVALID_JSON_EXAMPLE = """
        {
          "status code": 400,
          "result": "Invalid JSON data: com.google.gson.stream.MalformedJsonException: Expected name at line 20 column 6 path $[0].links[0].type",
          "status message": "FATAL"
        }
    """;

    public static final String PRODUCER_RESPONSE_400_INVALID_PROTOCOL_EXAMPLE = """
        {
          "status code": 400,
          "result": "No protocol service has been found registered",
          "status message": "FAIL"
        }
    """;

    public static final String PRODUCER_RESPONSE_404_EXAMPLE = """
        {
          "status code": 404,
          "result": "FAIL",
          "status message": "RabbitMQ properties not found"
        }
    """;

    public static final String PRODUCER_RESPONSE_500_EXAMPLE = """
        {
          "timestamp": "Mar 25, 2026, 1:09:26 PM",
          "status": 500,
          "error": "Internal Server Error",
          "path": "/producer/msg"
        }
    """;

    public static final String PRODUCER_RESPONSE_500_INVALID_EVENT_TYPE_EXAMPLE = """
        {
          "events": [
            {
              "status_code": 500,
              "result": "Internal Server Error",
              "message": "Could not prepare Routing key to publish message"
            }
          ]
        }
    """;

    // Request body example for /producer/msg API endpoint

    public static final String PRODUCER_REQUEST_INPUT_EXAMPLE = """
        {
          "meta": {
            "type": "EiffelActivityFinishedEvent",
            "id": "aaaaaaaa-bbbb-5ccc-8ddd-eeeeeeeeeee0",
            "version": "",
            "time": 1495061797000,
            "tags": [],
            "source": {
              "serializer": "pkg:maven/com.github.eiffel-community/eiffel-remrem-semantics@2.4.4"
            }
          },
          "data": {
            "outcome": {
              "conclusion": "SUCCESSFUL"
            },
            "persistentLogs": [],
            "customData": []
          },
          "links": [
            {
              "type": "ACTIVITY_EXECUTION",
              "target": "aaaaaaaa-bbbb-5ccc-8ddd-eeeeeeeeeee1"
            }
          ]
        }
    """;

    // Response examples for /generateAndPublish API endpoint

    public static final String GENERATE_PUBLISH_RESPONSE_200_EXAMPLE = """
    [
      {
        "result": {
          "events": [
            {
              "id": "52b495b3-b2c2-4439-81a2-79d2c2ed8e08",
              "status_code": 200,
              "result": "SUCCESS",
              "message": "Event sent successfully"
            }
          ]
        }
      }
    ]
    """;

    public static final String GENERATE_PUBLISH_RESPONSE_400_VALIDATION_EXAMPLE = """
    [
      {
        "status message": {
          "status code": 400,
          "result": "FAIL",
          "message": {
            "message": "Cannot validate given JSON string",
            "cause": "com.ericsson.eiffel.remrem.semantics.validator.EiffelValidationException: [object has missing required properties ([\\"conclusion\\"])]"
          }
        }
      }
    ]
    """;

    public static final String GENERATE_PUBLISH_RESPONSE_400_INVALID_PROTOCOL_EXAMPLE = """
        {
          "status code": 400,
          "result": "No protocol service has been found registered",
          "status message": "FAIL"
        }
    """;


    public static final String GENERATE_PUBLISH_RESPONSE_400_BAD_REQUEST_EXAMPLE = """
        [
          {
            "status message": {
              "timestamp": "Mar 25, 2026, 12:59:35 PM",
              "status": 404,
              "error": "Not Found",
              "path": "/wrong/eiffelsemantics"
            }
          }
        ]
    """;

    public static final String GENERATE_PUBLISH_RESPONSE_404_EXAMPLE = """
        {
          "status code": 404,
          "result": "FAIL",
          "status message": "Not found"
        }
    """;

    public static final String GENERATE_PUBLISH_RESPONSE_500_EXAMPLE = """
        {
          "timestamp": "Mar 25, 2026, 1:30:35 PM",
          "status": 500,
          "error": "Internal Server Error",
          "path": "/generateAndPublish"
        }
    """;

    // Request body example for /generateAndPublish API endpoint

    public static final String GENERATE_PUBLISH_REQUEST_INPUT_EXAMPLE = """
        {
            "msgParams": {"meta": {"type": "EiffelActivityFinishedEvent"}},
            "eventParams": {
                "data": {"outcome": {"conclusion": "SUCCESSFUL"}},
                "links": [{"type":"ACTIVITY_EXECUTION", "target": "aaaaaaaa-bbbb-5ccc-8ddd-eeeeeeeeeee1"}]
            }
        }
    """;

    // Response examples for /versions API endpoint

    public static final String VERSIONS_RESPONSE_200_EXAMPLE = """
        {
          "serviceVersion": {
            "serviceVersion": "0.0.1"
          },
          "endpointVersions": {
            "semanticsVersion": "0.0.1"
          }
        }
    """;

    private RemRemPublishResponseExamples() {}
}
