# Status Codes
The response generated will have internal status codes for each and every event based on the input JSON provided.

Status codes are generated according to the below tables.

## Status codes

Status codes returned by `/versions` and `/producer/msg` operations. 
Note, that status codes of operation `/generateAndPublish` are listed [below](#status-codes-related-to-failures-in-eiffel-remrem-generate-service).

| Status code | Result                        | Message                                                                             | Comment                                                                                                                                                                                                                                                                                                                                                                       |
|-------------|-------------------------------|-------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 200         | SUCCESS                       | Event sent successfully                                                             | Is returned if the request is completed successfully.                                                                                                                                                                                                                                                                                                                         |
| 207         | Multi-Status                  |                                                                                     | Is returned if we have a mix of internal status codes.  Eg: publish of few events was successful and few events was a failure.  In case of multiple events in the body JSON, if a event is given internal status code of 400 or 500 rest events will not be published and will be given a internal status code of 503, which will result in overall HTTP response code of 207. |
| 400         | Bad Request                   | Invalid event content, client need to fix problem in event before submitting again. | Is returned if the request body JSON is malformed. Eg: unable to parse the body JSON.                                                                                                                                                                                                                                                                                         |
| 404         | RabbitMQ properties not found | RabbitMQ properties not configured for the protocol <protocol>                      | Is returned if RabbitMQ message broker properties are not found for the protocol used by event.                                                                                                                                                                                                                                                                               |
| 415         | Unsupported Media Type        | Content type `<content-type>` not supported                                         | Indicates that the server refuses to accept the request because the payload format is in an unsupported format.                                                                                                                                                                                                                                                                                                                                                                             |
| 500         | Internal Server Error         | RabbitMQ is down. Please try later                                                  | Is returned if RabbitMQ is down.                                                                                                                                                                                                                                                                                                                                              |
|             |                               | Could not prepare Routing key to publish message.                                   | Is returned if could not prepare routing key to publish the eiffel event.                                                                                                                                                                                                                                                                                                     |
| 503         | Service Unavailable           | Please check previous event and try again later                                     | Is returned if there is a failure in publishing previous event with status code 400, 404 or 500.                                                                                                                                                                                                                                                                              |

### Status codes explanation

**200 OK**

All the events are sent successfully.

```
[
    {
     "id": "9cdd0f68-df85-44b0-88bd-fc4163ac90a0",
     "status_code": 200,
     "result": "SUCCESS",
     "message": "Event sent successfully"
    },
    {
     "id": "9cdd0f68-df85-44b0-88bd-fc4163ac90a1",
     "status_code": 200,
     "result": "SUCCESS",
     "message": "Event sent successfully"
    }
]
```

**207 Multi-Status**

Events are having different internal status codes.

```
[
    {
     "id": "9cdd0f68-df85-44b0-88bd-fc4163ac90a0",
     "status_code": 200,
     "result": "SUCCESS",
     "message": "Event sent successfully"
    },
    {
     "status_code": 400,
     "result": "Bad Request",
     "message": "Invalid event content, client need to fix problem in event before submitting again"
    }
]
```

**400 Bad Request**

The input JSON is malformed.

```
[
    {
     "status_code": 400,
     "result": "Bad Request",
     "message": "Invalid event content, client need to fix problem in event before submitting again"
    }
]
```

**404 Not Found**

RabbitMQ properties not configured in tomcat/conf/config.properties file for the protocol.

```
[
    {
     "status_code": 404,
     "result": "RabbitMQ properties not found",
     "message": "RabbitMQ properties not configured for the protocol <protocol>"
    }
]
```

**415 Unsupported Media Type**

Server refuses to accept the request because the payload format is in an unsupported format.

```
{
  "timestamp": "Sep 9, 2022 2:56:07 PM",
  "status": 415,
  "error": "Unsupported Media Type",
  "message": "Content type \u0027unsupported;charset\u003dUTF-8\u0027 not supported",
  "path": "/publish/producer/msg"
}
```

**500 Internal Server Error**

Cannot prepare routing key.

```
[
    {
     "status_code": 500,
     "result": "Internal Server Error",
     "message": "Could not prepare Routing key to publish message"
    }
]
```

Event is failed to send because of internal server error.

```
[
    {
     "id": "9cdd0f68-df85-44b0-88bd-fc4163ac90a0",
     "status_code": 500,
     "result": "Internal Server Error",
     "message": "RabbitMQ is down. Please try later"
    }
]
```

**503 Service Unavailable**

Event will have this status code when the previous event is failed to send.

For example, consider you have 5 events: **E1<-E2<-E3<-E4<-E5**
You send them all to publish in a batch command.
**E1** and **E2** are sent successfully **(200 OK)**.
**E3** is malformed **(400 Bad Request)**.
If we send **E4** and **E5** we will have a broken chain so that is not a good option.
In this case we should skip sending **E4** and **E5** and have status code **503** for those.

```
[
     {
      "id": "9cdd0f68-df85-44b0-88bd-fc4163ac90a0",
      "status_code": 200,
      "result": "SUCCESS",
      "message": "Event sent successfully"
     },
     {
      "id": "9cdd0f68-df85-44b0-88bd-fc4163ac90a1",
      "status_code": 200,
      "result": "SUCCESS",
      "message": "Event sent successfully"
     },
     {
      "status_code": 400,
      "result": "Bad Request",
      "message": "Invalid event content, client need to fix problem in event before submitting again"
     },
     {
      "status_code": 503,
      "result": "Service Unavailable",
      "message": "Please check previous event and try again later"
     },
     {
      "status_code": 503,
      "result": "Service Unavailable",
      "message": "Please check previous event and try again later"
     }
]
```

**NOTE:** you can open the RabbitMQ's management console and find these messages in a queue.

## Status codes related to failures in Eiffel REMReM Generate Service

These response can be generated only when `/generateAndPublish` endpoint is used.

| Status code | Result                | Message                                                                     | Comment                                                                                                                |
|-------------|-----------------------|-----------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| 400         | Bad Request           | Malformed JSON or incorrect type of event                                   | Is returned if the request body JSON is malformed or entered incorrect type of event.                                  |
| 401         | Unauthorized          | Unauthorized. Please, check if LDAP for REMReM Generate Service is disabled | Is returned if LDAP for REMReM Generate Service is enabled and REMReM Generate Publish have not access to it.          |
| 406         | Not Acceptable        | No event id found with ERLookup properties                                  | Is returned if no event id fetched from configured event repository in REMReM generate.                                |
| 415         | Unsupported Media Type| Content type `<content-type>` not supported                                         | Indicates that the server refuses to accept the request because the payload format is in an unsupported format.                                                                                                                                                                                                                                                                                                                                                                             |
| 417         | Expectation Failed    | Multiple event ids found with ERLookup properties                           | Is returned if multiple event ids fetched from configured event repository in REMReM generate.                         |
| 422         | Unprocessable Entity  | Link specific lookup options could not be fulfilled                         | Is returned if Link specific lookup options could not be matched with failIfMultipleFound and failIfNoneFound.         |
| 500         | Internal Server Error | Internal server error in Generate Service                                   | Is returned if REMReM Generate Service is not started or in case of others internal errors in REMReM Generate Service. |
| 503         | Service Unavailable   | No protocol service has been found registered                               | Is returned if there is no such message protocol loaded.                                                               |

### Status codes explanation

**400 Bad Request**

The input JSON is malformed or entered incorrect type of event.

```
[
    {
     "status_code": 400,
     "result": "FAIL",
     "message": "Malformed JSON or incorrect type of event"
    }
]
```

**401 Unauthorized**

LDAP for REMReM Generate Service is enabled and REMReM Generate Publish have not access to it.

```
[
    {
     "status_code": 401,
     "result": "FAIL",
     "message": "Unauthorized. Please, check if LDAP for REMReM Generate Service is disabled"
    }
]
```

**406 Not Acceptable**

The Lookup properties with no event id fetched from configured event repository in generate , REMReM fails to generate.

```
[
    {
     "status_code": 406,
     "result": "FAIL",
     "message": "No event id found with ERLookup properties"
    }
]
```

**415 Unsupported Media Type**

Server refuses to accept the request because the payload format is in an unsupported format.

```
{
  "timestamp": "Sep 9, 2022 2:56:07 PM",
  "status": 415,
  "error": "Unsupported Media Type",
  "message": "Content type \u0027unsupported;charset\u003dUTF-8\u0027 not supported",
  "path": "/publish/producer/msg"
}
```


**417 Expectation Failed**

The Lookup properties with multiple event ids fetched from configured event repository in generate , REMReM fails to generate.

```
[
    {
     "status_code": 417,
     "result": "FAIL",
     "message": "Multiple event ids found with ERLookup properties"
    }
]
```

**422 Unprocessable Entity**

The link specific lookup options could not be matched with failIfMultipleFound and failIfNoneFound in generate , REMReM fails to generate.

```
[
    {
     "status_code": 422,
     "result": "FAIL",
     "message": "Link specific lookup options could not be fulfilled"
    }
]
```

**500 Internal Server Error**

REMReM Generate Service is not started or in case of others internal errors in REMReM Generate Service.

```
[
    {
     "status_code": 500,
     "result": "FAIL",
     "message": "Internal server error in Generate Service"
    }
]
```

**503 Service Unavailable**

There is no such message protocol loaded..

```
[
     {
      "status_code": 503,
      "result": "FAIL",
      "message": "No protocol service has been found registered"
     }
]
```
