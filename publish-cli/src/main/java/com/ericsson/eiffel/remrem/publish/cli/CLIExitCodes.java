/*
    Copyright 2017 Ericsson AB.
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
package com.ericsson.eiffel.remrem.publish.cli;

import org.apache.commons.cli.MissingOptionException;

public class CLIExitCodes {
    public static int CLI_EXCEPTION=1;
    public static int CLI_MISSING_OPTION_EXCEPTION=2;
    public static int HANDLE_CONTENT_FAILED=3;
    public static int HANDLE_CONTENT_FILE_NOT_FOUND_FAILED=4;
    public static int HANDLE_CONTENT_FILE_COULD_NOT_READ_FAILED=5;
    public static int READ_JSON_FROM_CONSOLE_FAILED=6;



    public static int getExceptionCode(Exception e) {
        if (e instanceof MissingOptionException) {
            return CLI_MISSING_OPTION_EXCEPTION;
        }
        return CLI_EXCEPTION;
    }
}
