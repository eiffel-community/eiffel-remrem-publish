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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Autowired
    private BuildProperties buildProperties;

    @Bean
    public OpenAPI customOpenAPI() {

        final StringBuilder remremDescription = new StringBuilder();
        remremDescription.append("REMReM (REST Mailbox for Registered Messages) Publish "
                        + "for publish validated Eiffel messages on a RabbitMQ message bus.");
        remremDescription.append("<a href=https://github.com/eiffel-community/eiffel-remrem-publish/blob/master/wiki/markdown/index.md>REMReM Publish documentation</a>");

        return new OpenAPI()
                .info(new Info()
                        .title("Eiffel REMReM Publish Service")
                        .description(remremDescription.toString())
                        .version(buildProperties.getVersion()))
                .openapi("3.1.0");
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("remrem-publish")
                .pathsToMatch("/**")
                .build();
    }

}
