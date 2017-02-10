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
