## Logging
REMReM Publish application logging is implemented using the logback-classic. It requires user to configure the logback.xml file.
When used without logback.xml log messages will look as below:

```
%PARSER_ERROR[wEx]%PARSER_ERROR[clr] %PARSER_ERROR[clr] %PARSER_ERROR[clr] %PARSER_ERROR[clr]

%PARSER_ERROR[clr] %PARSER_ERROR[clr] %PARSER_ERROR[clr]
```

To configure the logback.xml file use -Dlogging.config=path/logback.xml

To get info about logback configurations see [here](https://logback.qos.ch/manual/configuration.html).