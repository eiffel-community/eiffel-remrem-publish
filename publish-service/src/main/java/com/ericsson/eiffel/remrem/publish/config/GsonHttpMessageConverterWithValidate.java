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

import java.io.Reader;
import java.lang.reflect.Type;

import org.apache.commons.io.IOUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;


/**
 * Ugly class that "overrides" private methods in order to get hold of the inputstream (the json body) so that we can
 * perform a check for duplicate keys.
 * <p>
 *
 * Gson does not support check for duplicate keys yet.
 * See: <a href="https://github.com/google/gson/issues/647">GitHub issue</a>
 */
public class GsonHttpMessageConverterWithValidate extends GsonHttpMessageConverter {

        private Gson gson;

        @Override
        public void setGson(final Gson gson) {
                super.setGson(gson);
                this.gson = gson;
        }

    @Override
    protected Object readInternal(Type resolvedType, Reader reader) throws Exception {
        try {
            final String json = IOUtils.toString(reader);
            // do the actual validation
            final ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            mapper.readTree(json);
            return this.gson.fromJson(json, resolvedType);
        } catch (JsonParseException ex) {
            throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
        }
        
    }

}
