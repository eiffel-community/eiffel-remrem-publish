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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
public class GsonHttpMessageConverterConfig {

        @Bean
        public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
                return new ByteArrayHttpMessageConverter();
        }

        @Bean
        public GsonHttpMessageConverter gsonHttpMessageConverter() {
                GsonHttpMessageConverterWithValidate converter = new GsonHttpMessageConverterWithValidate();
                converter.setGson(gson());
                return converter;
        }

        private Gson gson() {
                return new GsonBuilder().create();
        }
}
