# REMReM Publish Service

REMReM Publish Service allows for sending messages to a topic-based exchange on a RabbitMQ Server.

Information about the REMReM Publish Service all endpoints can be got and easily accessed using next links:

```
http://<host>:<port>/<application name>/
```

or

```
http://<host>:<port>/<application name>/swagger-ui.html
```

Example:

```
http://localhost:8080/publish/
```

## Configuration

### Running and configuring RemRem-Publish without Tomcat installation

It is also possible to execute RemRem-Publish without external Tomcat installation and using the embedded tomcat in Spring application instead.

If RemRem-Publish is executed without external Tomcat installation, the configuration should be based on Spring application properties, see publish-service/src/main/resources/application.properties file for available properties.

If running with java command, example:

```
java -jar publish-service/target/publish-service-x.x.x.war --properties.parameter1=value --properties.parameter2=value --properties.parameter3=value ......
```

It is also possible to provide path to the spring properties file:

```
java -jar publish-service/target/publish-service-x.x.x.war --spring.config.location=path/to/application.properties
```

Second option to execute RemRem-Publish is to execute maven commands from the source code root folder, example:

```
mvn spring-boot:run -Dspring-boot.run.arguments=--properties.parameter1=value,--properties.parameter2=value,--properties.parameter3=value,--properties.parameter4=value ......
```

When using maven command, Spring properties can also be changed by editing publish-service/src/main/resources/application.properties file.

Application launch is terminated if exchange is unavailable and createExchangeIfNotExisting is set to false. Application can create exchange by adding createExchangeIfNotExisting property in existing rabbitmq.instances.jsonlist in application.properties file.

eg: rabbitmq.instances.jsonlist=[{"existing properties": "values","createExchangeIfNotExisting":false }].

### Running and configuring in Tomcat

Eiffel REMReM Service publish-service.war file is deployed in Tomcat Server. For doing this, publish-service.war file should deployed in directory: *tomcat/webapps*.

Configuration is done in Tomcat using a application.properties file: *tomcat/conf/application.properties*.

The application.properties configuration file should be passed via environmental variables.

Set Java property *"SPRING_CONFIG_LOCATION=%CATALINA_HOME%\conf\application.properties"* in setenv file in bin folder.

Start the application from Tomcat. This will read the application.properties which are given in conf folder.

**NOTE**: in each example assuming the publish-service.war is deployed in tomcat as **publish**.

### Running and configuring in Tomcat in Linux

If you are using linux machine, please follow the below steps

Add the file *application.properties* to *${CATALINA_HOME}/conf*

Add export *JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=/path/to/file/application.properties"* in setenv.sh in *tomcat/bin*

Set the *System_JAVA_OPTS* as set *JAVA_OPTS = "-Dspring.config.location=$path/to/application.properties"* in catalina.sh in bin folder

Start the application from Tomcat.

### Jasypt configurations

Jasypt Spring Boot provides Encryption support for property sources(passwords, secret info ..etc) in Spring Boot Applications. To support this functionality in our application we need to add the following property in property file

```
jasypt.encryptor.jasyptKeyFilePath: The path of jasypt.key file containing the key value which was used while encrypting the original password
```

The key value in jasypt.key file must be same for both encryption and decryption of the original password(ldap,rabbitmq...etc)
The encryptor password will be used by jasypt-spring-boot library in application to decrypt the encrypted password at runtime.

### How to encrypt the password:

```
1) Download the jasypt jar file from any of the below locations
Link to [Jasypt](http://www.jasypt.org/download.html) (or) [Link to Maven](https://mvnrepository.com/artifact/org.jasypt/jasypt/1.9.2)

2) Execute the below command to generate Jasypt encrypted password
    java -cp jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="any password" password=any intermediate key

input   : any password which we want to encrypt(Ex: rabbitmq password, ldap user password, etc...)
password: A Jasypt key used to encrypt the above input( The Jasypt key can be anything, but make sure same key to be used for decryption)

Example:
    Run the below command in Command line

    java -cp jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="dummyPassword" password=dummy

output:
    ----ENVIRONMENT-----------------

    Runtime: Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 25.144-b01

    ----ARGUMENTS-------------------

    algorithm: PBEWithMD5AndDES
    input: dummyPassword
    password: dummy

    ----OUTPUT----------------------

    euJcvto7NtCDiWT7BKFW0A==
```

Use the above encrypted password in your property file like this **{ENC(encrypted password)}**

`Ex:rabbitmq.password: {ENC(euJcvto7NtCDiWT7BKFW0A==)}`

**Note: REMReM will work without jasypt encryption also but if you have encrypted any text using jasypt library then jasypt.encryptor.password property should be present in property file.**

### Exchange configurations

These parameters are related to RabbitMQ Server, which will be used for publishing events:

```
<protocol>.rabbitmq.host:          <host name, eg: localhost>
<protocol>.rabbitmq.port:          <port, eg: 5672>
<protocol>.rabbitmq.virtualHost:   <virtual host, eg: /eiffel/production, is optional>
<protocol>.rabbitmq.username:      <username, default for RabbitMQ Server: guest>
<protocol>.rabbitmq.password:      <password, default for RabbitMQ Server: guest>
<protocol>.rabbitmq.tls:           <tls version, is optional>
<protocol>.rabbitmq.exchangeName:  <exchange name, exchange should be already created on RabbitMQ Server>
<protocol>.rabbitmq.createExchangeIfNotExisting:  <create Exchange if not present on RabbitMQ Server>
<protocol>.rabbitmq.domainId:      <domain id, any string>
<protocol>.rabbitmq.channelsCount: <channels count, eg: 1 (default value is 1)>
<protocol>.rabbitmq.tcpTimeOut:    <tcp connection timeout value, eg: 5000 milliseconds (if value is mentioned as 0 than it will consider default value 60000 milliseconds)>
<protocol>.rabbitmq.waitForConfirmsTimeOut: <wait for confirms time out, eg: 5000 (default value is 5000)>
```

```
#RabbitMq configurations

# need to be updated according to the test env.
#rabbitmq.host=http://127.0.0.1
# must exist
#rabbitmq.exchange.name=eiffel.xxx

rabbitmq.instances.jsonlist=[{ "mp": "eiffelsemantics", "host": "127.0.0.1", "port": "5672", "virtualHost": "", "username": "guest", "password": "guest", "tls": "", "exchangeName": "amq.direct", "domainId": "eiffelxxx", "channelsCount": "1" ,"createExchangeIfNotExisting":true ,"waitForConfirmsTimeOut":"5000", "tcpTimeOut": "5000" }, \
{ "mp": "eiffelprotocol", "host": "127.0.0.1", "port": "5672", "virtualHost": "", "username": "guest", "password": "guest", "tls": "", "exchangeName": "amq.direct", "domainId": "eiffelxxx", "channelsCount": "1" ,"createExchangeIfNotExisting":true,"waitForConfirmsTimeOut":"5000", "tcpTimeOut": "5000"  }]
```

Application launch is terminated if exchange is unavailable and createExchangeIfNotExisting is set to false. Application can create the exchange by adding the below property in config.properties file

eg: protocol.rabbitmq.createExchangeIfNotExisting: true.

`<protocol>` is name of the protocol used (eg: `eiffelsemantics`).

**NOTE:** properties above should be configured for each protocol, that users are going to use.

```
activedirectory.publish.enabled:    <true|false>
activedirectory.ldapUrl:            <LDAP server url>
activedirectory.managerPassword:    <LDAP server manager password >
activedirectory.managerDn:          <LDAP managerDn pattern>
activedirectory.rootDn:             <LDAP rootDn pattern>
activedirectory.userSearchFilter:   <LDAP userSearchFilter pattern>
activedirectory.connectionTimeOut:  <LDAP connection timeout value>
```

**LDAP authentication without Base64 encryption of user details:**

```
curl -XPOST -H "Content-Type: application/json" --user username:password --data @ActivityCanceled.json "http://localhost:8080/publish/producer/msg?mp=eiffelsemantics"
```

**NOTE:** each HTTP request must then include an Authorization header with value Basic `<Base64 encoded username:password>`.

**LDAP authentication with Base64 encryption of user details:**

```
curl -XPOST -H "Content-Type: application/json" -H 'Authorization: Basic cGFzc3dvcmQ=' --data @ActivityCanceled.json "http://localhost:8080/publish/producer/msg?mp=eiffelsemantics"
```

### Generate Service configurations for Publish Service

Properties below are important for correct work of `/generateAndPublish` endpoint.

```
generate.server.host:    <host, where generate service is deployed, eg: localhost>
generate.server.port:    <port, eg: 8080>
generate.server.appName: <application name of generate service, eg: generate>
```

## Usage

Available REST resources for REMReM Publish Service are described below.

| Resource            | Method | Parameters                                                                                                                                                                             | Request body                                                                                                                                                                                                    | Description                                                                                                                                                                                                                                                                                           |
|---------------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| /producer/msg       | POST   | mp - message protocol, required msgType - message type, required ud - user domain, not required tag - not required rk - routing key, not required                                      | {   "meta": {     # Matches the meta object   },   "data": {     # Matches the data object   },   "links": {     # Matches the links object   } }                                                               | This endpoint is used to publish already generated Eiffel event to message bus. **Note:** This endpoint will not validate the message. It will check only if the message contains eventId and eventType.                                                                                                                                                                                                                      |
| /generateAndPublish | POST   | mp - message protocol, required ud - user domain, not required tag - not required rk - routing key, not required failIfMultipleFound - default: false, failIfNoneFound - default: false,  lookupInExternalERs - default: false, lookupLimit - default: 1| {   "msgParams": {     "meta": {       # Matches the meta object     }   },   "eventParams": {     "data": {       # Matches the data object     },     "links": {       # Matches the links object     }   } } | This endpoint is used to generate and publish Eiffel events to message bus. It provides single endpoint for both REMReM Generate and REMReM Publish. The service works on the relative link /generateAndPublish if run as standalone application or /publish/generateAndPublish if run as tomcat app. |
| /versions           | GET    |                                                                                                                                                                                        |                                                                                                                                                                                                                 | This endpoint is used to get versions of publish service and all loaded protocols.                                                                                                                                                                                                                    |

**generateAndPublish endpoint is provided with four options for Lookups:**

| Options              | Default Value | Description                                                                                                                                                                                                                                                                                                                                                                                                                                |
|----------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| failIfMultipleFound: | False         | If value is set to True and multiple event ids are found through any of the provided lookup definitions, then no event will be generated. |
| failIfNoneFound:     | False         | If value is set to True and no event id is found through (at least one of) the provided lookup definitions, then no event will be generated. |
| lookupInExternalERs:             | False          | If value is set to True then REMReM will query external ERs and not just the locally used ER. The reason for the default value to be False is to decrease the load on external ERs. Here local ER means Single ER which is using REMReM generate.  External ER means multiple ER's which are configured in Local ER.|
| lookupLimit:            | 1             | The number of events returned, through any lookup definition given, is limited to this number. |

## Examples

Typical examples of usage Eiffel REMReM Publish Service endpoints are described below.

You can use command line tools like [curl](https://curl.haxx.se/) or some plugin for your favorite browser. For example:

* [Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop) for Chromium-based browsers
* [HttpRequester](https://addons.mozilla.org/en-US/firefox/addon/httprequester/) for Firefox

### Examples for /producer/msg endpoint

**One message:**

```
curl -H "Content-Type: application/json" -X POST -d '[{"meta":{"id":"963da060-f2cf-4370-a68d-67fd872def36","type":"EiffelActivityFinishedEvent","version":"3.0.0","time":1513758440588,"tags": ["product_master","product_feature1"], "source": {"domainId": "eiffeltest", "host": "localhost", "name": "LOCALHOST", "uri": "http://localhost:8080/jenkins","serializer":"pkg:maven/com.github.eiffel-community/eiffel-remrem-semantics@2.0.2"}}, "eventParams": {"data": {"outcome": {"conclusion": "SUCCESSFUL"}, "persistentLogs": [{"name": "firstLog", "uri": "http://localhost:8080/firstLog"}, {"name": "otherLog", "uri": "http://localhost:8080/otherlogs"}]}, "links": [{"type": "ACTIVITY_EXECUTION", "target": "e269b37d-17a1-4a10-aafb-c108735ee51f"}]}}]' "http://localhost:8080/publish/producer/msg?mp=eiffelsemantics"
```

Result:

```
{"events":[{"id":"963da060-f2cf-4370-a68d-67fd872def36","status_code":200,"result":"SUCCESS","message":"Event sent successfully"}]}
```

**Two messages/objects and given user domain suffix:**

```
curl -H "Content-Type: application/json" -X POST -d '[{"meta":{"id":"963da060-f2cf-4370-a68d-67fd872def36","type":"EiffelActivityFinishedEvent","version":"3.0.0","time":1513758440588,"tags": ["product_master","product_feature1"], "source": {"domainId": "eiffeltest", "host": "localhost", "name": "LOCALHOST", "uri": "http://localhost:8080/jenkins","serializer":"pkg:maven/com.github.eiffel-community/eiffel-remrem-semantics@2.0.2"}}, "eventParams": {"data": {"outcome": {"conclusion": "SUCCESSFUL"}, "persistentLogs": [{"name": "firstLog", "uri": "http://localhost:8080/firstLog"}, {"name": "otherLog", "uri": "http://localhost:8080/otherlogs"}]}, "links": [{"type": "ACTIVITY_EXECUTION", "target": "e269b37d-17a1-4a10-aafb-c108735ee51f"}]}},{"meta":{"id":"963da060-f2cf-4370-a68d-67fd872dek89","type":"EiffelActivityFinishedEvent","version":"3.0.0","time":1513758440588,"tags": ["product_master","product_feature1"], "source": {"domainId": "eiffeltest", "host": "localhost", "name": "LOCALHOST", "uri": "http://localhost:8080/jenkins","serializer":"pkg:maven/com.github.eiffel-community/eiffel-remrem-semantics@2.0.2"}}, "eventParams": {"data": {"outcome": {"conclusion": "SUCCESSFUL"}, "persistentLogs": [{"name": "firstLog", "uri": "http://localhost:8080/firstLog"}, {"name": "otherLog", "uri": "http://localhost:8080/otherlogs"}]}, "links": [{"type": "ACTIVITY_EXECUTION", "target": "e269b37d-17a1-4a10-aafb-c108735ee51f"}]}}]' "http://localhost:8080/publish/producer/msg?ud=fem001&mp=eiffelsemantics"
```

Result:

```
{"events":[{"id":"963da060-f2cf-4370-a68d-67fd872def36","status_code":200,"result":"SUCCESS","message":"Event sent successfully"},{"id":"963da060-f2cf-4370-a68d-67fd872dek89","status_code":200,"result":"SUCCESS","message":"Event sent successfully"}]}
```

**One message and given tag:**

```
curl -H "Content-Type: application/json" -X POST -d '[{"meta":{"id":"963da060-f2cf-4370-a68d-67fd872def36","type":"EiffelActivityFinishedEvent","version":"3.0.0","time":1513758440588,"tags": ["product_master","product_feature1"], "source": {"domainId": "eiffeltest", "host": "localhost", "name": "LOCALHOST", "uri": "http://localhost:8080/jenkins","serializer":"pkg:maven/com.github.eiffel-community/eiffel-remrem-semantics@2.0.2"}}, "eventParams": {"data": {"outcome": {"conclusion": "SUCCESSFUL"}, "persistentLogs": [{"name": "firstLog", "uri": "http://localhost:8080/firstLog"}, {"name": "otherLog", "uri": "http://localhost:8080/otherlogs"}]}, "links": [{"type": "ACTIVITY_EXECUTION", "target": "e269b37d-17a1-4a10-aafb-c108735ee51f"}]}}]' "http://localhost:8080/publish/producer/msg?mp=eiffelsemantics&tag=production"
```

Result:

```
{"events":[{"id":"963da060-f2cf-4370-a68d-67fd872def36","status_code":200,"result":"SUCCESS","message":"Event sent successfully"}]}
```

**One message and given routing key:**

```
curl -H "Content-Type: application/json" -X POST -d '[{"meta":{"id":"963da060-f2cf-4370-a68d-67fd872def36","type":"EiffelActivityFinishedEvent","version":"3.0.0","time":1513758440588,"tags": ["product_master","product_feature1"], "source": {"domainId": "eiffeltest", "host": "localhost", "name": "LOCALHOST", "uri": "http://localhost:8080/jenkins","serializer":"pkg:maven/com.github.eiffel-community/eiffel-remrem-semantics@2.0.2"}}, "eventParams": {"data": {"outcome": {"conclusion": "SUCCESSFUL"}, "persistentLogs": [{"name": "firstLog", "uri": "http://localhost:8080/firstLog"}, {"name": "otherLog", "uri": "http://localhost:8080/otherlogs"}]}, "links": [{"type": "ACTIVITY_EXECUTION", "target": "e269b37d-17a1-4a10-aafb-c108735ee51f"}]}}]' "http://localhost:8080/publish/producer/msg?mp=eiffelsemantics&rk=myroutingkey"
```

Result:

```
{"events":[{"id":"963da060-f2cf-4370-a68d-67fd872def36","status_code":200,"result":"SUCCESS","message":"Event sent successfully"}]}
```

**NOTE:** for any protocol, provide the Java opts as:

```
set JAVA_OPTS="-Djava.ext.dirs=/path/to/jars/" in catalina.sh
```

**NOTE:** in the above example, protocol jar file must be present inside "/path/to/jars/" folder.

**Reading data from a file:**

```
curl -H "Content-Type: application/json" -X POST --data-binary "@test.data" "http://localhost:8080/publish/producer/msg?mp=eiffelsemantics"
```

Result:

```
{"events":[{"id":"963da060-f2cf-4370-a68d-67fd872def36","status_code":200,"result":"SUCCESS","message":"Event sent successfully"},{"id":"963da060-f2cf-4370-a68d-67fd872dek89","status_code":200,"result":"SUCCESS","message":"Event sent successfully"}]}
```

**Malformed input:**

```
curl -H "Content-Type: application/json" -X POST -d '[{Message}]' "http://localhost:8080/publish/producer/msg?ud=fem001&mp=eiffelsemantics"
```

Result:

```
{"timestamp":"Dec 2, 2021 7:44:31 PM","status":400,"error":"Bad Request","message":"Could not read JSON: ..."}
```

### Examples for /generateAndPublish endpoint

**Correct message:**

```
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{"msgParams": {"meta": {"type": "EiffelActivityTriggeredEvent","version": "4.0.0","tags": [""],"source": {"domainId": "","host": "","name": "","uri": ""}}},"eventParams": {"data": {"name": "eiffel-generic-site","categories": [],"triggers": [{"type": "EIFFEL_EVENT","description": "Triggered by Eiffel message"}],"customData": []},"links": []}}' 'http://localhost:8080/publish/generateAndPublish?mp=eiffelsemantics&msgType=EiffelActivityTriggeredEvent&parseData=false&failIfMultipleFound=false&failIfNoneFound=false&lookupInExternalERs=false&lookupLimit=1&okToLeaveOutInvalidOptionalFields=false'
```

Result:

```
{"events":[{"id":"963da060-f2cf-4370-a68d-67fd872def36","status_code":200,"result":"SUCCESS","message":"Event sent successfully"}]}
```

**Incorrect message protocol:**

```
curl -H "Content-Type: application/json" -X POST -d '{"msgParams":  {"meta": {"type": "EiffelActivityFinishedEvent", "version": "3.0.0","tags": ["product_master","product_feature1"], "source": {"domainId": "eiffeltest", "host": "localhost", "name": "LOCALHOST", "uri": "http://localhost:8080/jenkins/","serializer":"pkg:maven/com.github.eiffel-community/eiffel-remrem-semantics@2.0.2"}}, "eventParams": {"data": {"outcome": {"conclusion": "SUCCESSFUL"}, "persistentLogs": [{"name": "firstLog", "uri": "http://localhost:8080/firstLog"}, {"name": "otherLog", "uri": "http://localhost:8080/otherlogs"}]}, "links": [{"type": "ACTIVITY_EXECUTION", "target": "e269b37d-17a1-4a10-aafb-c108735ee51f"}]}}}' "http://localhost:8080/publish/generateAndPublish?mp=incorrecteiffelprotocol&msgType=EiffelActivityFinished"
```

Result:

```
{"status_code":503,"result":"FAIL","message":"No protocol service has been found registered"}
```

**Incorrect message type:**

```
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{"msgParams": {"meta": {"type": "EiffelActivityTriggeredEvent","version": "4.0.0","tags": [""],"source": {"domainId": "","host": "","name": "","uri": ""}}},"eventParams": {"data": {"name": "eiffel-generic-site","categories": [],"triggers": [{"type": "EIFFEL_EVENT","description": "Triggered by Eiffel message"}],"customData": []},"links": []}}' 'http://localhost:8080/publish/generateAndPublish?mp=eiffelsemantics&msgType=incorrectmessagetype&parseData=false&failIfMultipleFound=false&failIfNoneFound=false&lookupInExternalERs=false&lookupLimit=1&okToLeaveOutInvalidOptionalFields=false'
```

Result:

```
{"result":"error","message":"Unknown event type requested..."}
```

### Examples for `/versions` endpoint

```
curl -H "Content-Type: application/json" -X GET http://localhost:8080/generate/versions
```

Result:

```
{"serviceVersion":{"version":"x.x.x"},"endpointVersions":{"semanticsVersion":"x.x.x"}}
```

## Status Codes
For each user request Eiffel REMReM Publish generate response in JSON with internal status code and message.

To get information about internal status codes see [here](../statusCodes.md).