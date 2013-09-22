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

# Declare the AuditLoggerFilter

AuditLoggerFilter is a ServletFilter that Dump full request and response to an SLF4J logger.

* RequestLoggerFilter.request  - Dumps the request if debug is enabled
* RequestLoggerFilter.response - Dumps the response if debug is enabled
* RequestLoggerFilter.headers  - Add Http Headers to request and response logs if debug is enabled

## Mapping sample

```
  <filter>
    <filter-name>auditLoggerFilter</filter-name>
    <filter-class>fr.xebia.extras.filters.logfilters.RequestLoggerFilter</filter-class>
  </filter>
  
  <!-- ... -->
    
  <filter-mapping>
      <filter-name>auditLoggerFilter</filter-name>
      <url-pattern>/rest/*</url-pattern>
  </filter-mapping>
```

## logback configuration

 ```
 <!--Dumping HTTP Requests and response using the RequestLoggerFilter -->
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
