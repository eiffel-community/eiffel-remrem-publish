
## 0.5.3
- Added swagger for publish service
- Changed year in copyright headers from 2017 to 2018

## 0.5.2
- Added a common REST API for Generate and Publish
- Moved ldap related functionality from shared
- Added functionality to disable the Remrem Publish Authentication.
- Corrected the name of endpoint "version" back to "versions"
- Updated github pages for common REST api for generate and publish
- Uplifted remrem-semantics version to 0.3.7

## 0.5.1
- This version is the same as 0.4.9. The intended changes were missed to be merged.
- Updated remrem-generator version to 0.5.1

## 0.5.0
- This version is the same as 0.4.9. The intended changes were missed to be merged.
- Uplifted remrem-semantics version to the latest version 0.3.2 to support continuous operation events

## 0.4.9
- Modified rk and tag to query parameters in Service.

## 0.4.8
- Uplifted remrem-semantics to 0.3.1 to override given input meta.source.serializer GAV information

## 0.4.7
- Added rk(Routing key) and tag option in CLI.
- Added routingKey and tag parameter in Service.
- Moved Routing key generation to corresponding protocol.

## 0.4.6
- Uplifted remrem-semantics version to 0.2.9

## 0.4.5
- Uplifted remrem-semantics version to 0.2.8.

## 0.4.4
- Uplifted remrem-semantics version to 0.2.7.

## 0.4.3
- Removed spring configurations from config.properties file and handled through code.

## 0.4.2
- Changed REMReM publish to read required properties from CLI options for CLI and from JAVA_OPTS, tomcat/conf/config.properties for service.

## 0.4.1
- Implemented functionality to get properties from java opts for publish-service

## 0.4.0
- Implemented one MB for each protocol in REMREM publish.

## 0.3.9
- Uplifted remrem-semantics version to 0.2.6 to support links validation.

## 0.3.8
- Added copyright headers to the source code.
- Uplifted remrem-shared version to 0.3.3 to get the versions of publish and all loaded protocols.

## 0.3.7
- Updated remrem-shared version to 0.3.2 to support base64 encryption functionality for Ldap manager password.

## 0.3.6
- Added validation for 503 status code to check routing key as null.

## 0.3.5
- Moved ldap related functionality to shared

## 0.3.4
- Updated Version for dependency Commons-Lang in REMREM publish

## 0.3.3
- Implemented RoutingKey functionality for REMREM Semantics.

## 0.3.2
- Added comments and removed unnecessary dependencies in build.gradle.

## 0.3.1
- Added changes in build.gradle.

## 0.3.0
- Split REMREM Publish component into cli, service and common.

## 0.2.9
- Updated the RemRem shared version to latest.

## 0.2.8
- Implemented RabbitMQ connection retry in Service.

## 0.2.7
- upgraded semantics version in build.gradle to support all
  EiffelEvents in the eiffel repo from topic-drop4 branch 

## 0.2.6
- Added status codes for output generate from publish.

## 0.2.5
- Added domain option in CLI

## 0.2.4
- Removed unused libraries and updated a few library versions.

## 0.2.3
- Implemented array of Json objects as input and output in Json format in CLI
- Added Debug flag

## 0.2.2
- Added eventId to output format in REMREM publish
- Implemented get route key from messaging library.

## 0.2.1
- Fix authentication required even when activedirectory.enabled=false

## 0.2.0
- Added SSL support for the RabbitMq connection.
- Improved Cli
- Added unit tests.

## 0.1.9
- added optional authentication to an Active Directory server for all 
  REST endpoints


## 0.1.8
- Improved error handling/logging
