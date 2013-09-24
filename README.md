<!--                                                                           -->
<!--  Copyright 2008-2010 Xebia and the original author or authors.            -->
<!--                                                                           -->
<!--  Licensed under the Apache License, Version 2.0 (the "License");          -->
<!--  you may not use this file except in compliance with the License.         -->
<!--  You may obtain a copy of the License at                                  -->
<!--                                                                           -->
<!--       http://www.apache.org/licenses/LICENSE-2.0                          -->
<!--                                                                           -->
<!--  Unless required by applicable law or agreed to in writing, software      -->
<!--  distributed under the License is distributed on an "AS IS" BASIS,        -->
<!--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. -->
<!--  See the License for the specific language governing permissions and      -->
<!--  limitations under the License.                                           -->
<!--                                                                           -->

[![Build Status](https://buildhive.cloudbees.com/job/xebia-france/job/xebia-logfilter-extras/badge/icon)](https://buildhive.cloudbees.com/job/xebia-france/job/xebia-logfilter-extras/)

# xebia-logfilter-extras

This project provide a ServletFilter that can dump request and response to the SLF4J API implementation in use.
When the logger has a DEBUG level, it will wrap request/response and intercept data from the body.
If level is higher than DEBUG, the filter will simply call the filter chain without doing anything.

Dumping can be enabled separately for request and response. To provide easy coupling between request and response
the filter will generate an Integer base ID when processing.

It is possible to limit the size of the dump using the maxDumpSizeInKB init parameter. This parameter
defines the max size of the dump in Kilo Bytes, beware that it defaults to 500KB.

This project depends only on SLF4J API, and servlet 2.5 API considered as provided. It is built using
Java 1.7 using code style 1.7 at compile time. If you need older code style or any other feature ask for
it using the issue tracker.

# Using the RequestLoggerFilter

RequestLoggerFilter is a ServletFilter that Dump full request and response to an SLF4J logger.

* RequestLoggerFilter.request  - Dumps the request if debug is enabled
* RequestLoggerFilter.response - Dumps the response if debug is enabled
* RequestLoggerFilter.headers  - Add Http Headers to request and response logs if debug is enabled

## Mapping sample

Here is an extract from a war web.xml, configuring the RequestLoggerFilter to be able to dump every
request and response on /rest/* URL pattern.

```
  <filter>
    <filter-name>requestLoggerFilter</filter-name>
    <filter-class>fr.xebia.extras.filters.logfilters.RequestLoggerFilter</filter-class>
    <init-param>
        <param-name>maxDumpSizeInKB</param-name>
        <param-value>500</param-value>
      </init-param>
  </filter>
  
  <!-- ... -->
    
  <filter-mapping>
      <filter-name>requestLoggerFilter</filter-name>
      <url-pattern>/rest/*</url-pattern>
  </filter-mapping>
```

## logback configuration

In this sample we use logback, but since we use slf4j, you can use any implementation of the API
to configure it. The main things to notice are the loggers names. Please also notice that headers
logger is only used to enable the dumping of the headers, no logs are appended to it.

 ```
<!--
 Dumping HTTP requests and response with their headers to the console appender
  using the RequestLoggerFilter
-->
    <logger name="RequestLoggerFilter.request" level="debug" additivity="false">
        <appender-ref ref="console"/>
    </logger>

    <logger name="RequestLoggerFilter.response" level="info" additivity="false">
        <appender-ref ref="console"/>
    </logger>

    <logger name="RequestLoggerFilter.headers" level="info" additivity="false">
        <appender-ref ref="console"/>
    </logger>
```

## Maven dependency

This project is available via maven central repository

### For release version:
```
<dependency>
  <groupId>fr.xebia.extras</groupId>
  <artifactId>xebia-logfilter-extras</artifactId>
  <version>1.0.0</version>
</dependency>
```

### For SNAPSHOT version:
```
<dependency>
  <groupId>fr.xebia.extras</groupId>
  <artifactId>xebia-logfilter-extras</artifactId>
  <version>1.0.1-SNAPSHOT</version>
</dependency>
```

