/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.extras.filters.logfilters;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter in charge of dumping requests and responses coming to webapp.
 *
 * This class defines 3 loggers for dumping requests :
 *      *   RequestLoggerFilter.headers  - higher than debug level, logs headers for request and response if request and/or response are to be dumped
 *      *   RequestLoggerFilter.request  - higher than debug level, logs the complete request
 *      *   RequestLoggerFilter.response - higher than debug level, logs the complete body of the response
 *
 * This filter does nothing if there is no dumping configured by log level.
 *
 * The Filter defines a maxDumpSizeInKB that allows to define a limit to the size of the payload or body it will dump in logs. This parameter is defined in KB
 * and defaults to 500 KB.
 */
public class RequestLoggerFilter implements Filter {


    private static final Logger logger = LoggerFactory.getLogger(RequestLoggerFilter.class);
    private static final Logger LOG_HEADERS = LoggerFactory.getLogger(RequestLoggerFilter.class.getSimpleName() + ".headers");
    private static final Logger LOG_REQUEST = LoggerFactory.getLogger(RequestLoggerFilter.class.getSimpleName() + ".request");
    private static final Logger LOG_RESPONSE = LoggerFactory.getLogger(RequestLoggerFilter.class.getSimpleName() + ".response");

    private static final AtomicInteger counter = new AtomicInteger();

    // limit Size in Ko of dumped body
    private int maxDumpSizeInKB = 500;
    static final int BUFFER_SIZE = 1024;

    public int getMaxDumpSizeInKB() {
        return maxDumpSizeInKB;
    }

    /**
     * Loads the maxDumpSizeInKB init parameter of the filter as defined in web.xml
     * maxDumpSizeInKB default value is 500 Ko. If parameter has a bad configuration (non positive integer or non integer String),
     * default value will be used.
     * @param filterConfig          configuration object loaded for this filter
     * @throws ServletException     Never throw this exception but it is needed by Servlet API
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Nothing to do on init.
        String maxDumpSizeStr = null;
        if (( maxDumpSizeStr = filterConfig.getInitParameter("maxDumpSizeInKB") )!= null) {

            try {
                int tempDumpSize = new Integer(maxDumpSizeStr).intValue();
                if (tempDumpSize > 0){
                    maxDumpSizeInKB = tempDumpSize;
                } else {
                    logger.warn("Bad format for maxDumpSizeInKB parameter expecting positive Integer value:{}", tempDumpSize);
                }
            } catch (NumberFormatException e) {
                logger.warn("Bad format for maxDumpSizeInKB parameter expecting positive Integer value:{}", maxDumpSizeStr, e);
            }
        }
        logger.warn("RequestLoggerFilter defined with maxDumpSizeInKB to {} KB", maxDumpSizeInKB);
    }

    @Override
    public void destroy() {
        // Nothing to do on destroy.
    }

    /**
     * This is where the work is done.
     *
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        ServletRequest request = servletRequest;
        ServletResponse response = servletResponse;
        int id = 0;

        // Generate the identifier if dumping is enabled for request and/or response
        if (LOG_REQUEST.isDebugEnabled() || LOG_RESPONSE.isDebugEnabled()) {
            id = counter.incrementAndGet();
        }

        // Dumping of the request is enabled so build the RequestWrapper and dump the request
        if (LOG_REQUEST.isDebugEnabled()) {
            request = new HttpServletRequestLoggingWrapper((HttpServletRequest) servletRequest, maxDumpSizeInKB);
            dumpRequest((HttpServletRequestLoggingWrapper) request, id);
        }

        if (LOG_RESPONSE.isDebugEnabled()) { // Dumping of the response is enabled so build the wrapper, handle the chain, dump the response, and write it to the outpuStream.
            response = new HttpServletResponseLoggingWrapper((HttpServletResponse) servletResponse, maxDumpSizeInKB);
            filterChain.doFilter(request, response);
            dumpResponse((HttpServletResponseLoggingWrapper) response, id);
            servletResponse.getOutputStream().write(((HttpServletResponseLoggingWrapper) response).getContentAsBytes());
        } else {
            // Dumping of the response is not needed so we just handle the chain
            filterChain.doFilter(request, response);
        }
    }


    /**
     * This method handles the dumping of the reponse body, status code and headers if needed
     * @param response      ResponseWrapper that handled the response populated by the webapp
     * @param id            Generated unique identifier for the request/response couple
     */
    private void dumpResponse(final HttpServletResponseLoggingWrapper response, final int id) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.print("-- Response ID: ");
        printWriter.println(id);
        printWriter.println(response.getStatusCode());
        if (LOG_HEADERS.isDebugEnabled()) {
            final Map<String, List<String>> headers = response.headers;
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                for (String value : header.getValue()) {
                    printWriter.print(header.getKey());
                    printWriter.print(": ");
                    printWriter.print(value);
                }
                printWriter.println();
            }
        }
        printWriter.println("--begin response body");
        printWriter.println(response.getContentAsInputString());
        printWriter.println("--end response body");
        printWriter.flush();
        LOG_RESPONSE.debug(stringWriter.toString());
    }

    /**
     * This method handles the dumping of the request body, method, URL and headers if needed
     * @param request       RequestWrapper used to handle the request by the webapp
     * @param id            Generated unique identifier for the request/response couple
     */
    private void dumpRequest(final HttpServletRequestLoggingWrapper request, final int id) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        if ((request.getRemoteUser() == null) || (request.getRemoteUser().trim().length() == 0)) {
            printWriter.print("Not authenticated");
        } else {
            printWriter.print("Authenticated as ");
            printWriter.println(request.getRemoteUser());
        }
        printWriter.print("-- Request ID:");
        printWriter.println(id);
        printWriter.print(request.getMethod());
        printWriter.print(" ");
        printWriter.print(request.getRequestURL());
        printWriter.print(" ");
        printWriter.println(request.getProtocol());
        if (LOG_HEADERS.isDebugEnabled()) {
            @SuppressWarnings("unchecked")
            final Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                final String key = headerNames.nextElement();
                @SuppressWarnings("unchecked")
                final Enumeration<String> headerValues = request.getHeaders(key);
                while (headerValues.hasMoreElements()) {
                    printWriter.print(key);
                    printWriter.print(": ");
                    printWriter.print(headerValues.nextElement());
                }
                printWriter.println();
            }
        }
        printWriter.println("--begin request body");
        printWriter.println(request.getBody());
        printWriter.println("--end request body");
        printWriter.flush();
        LOG_REQUEST.debug(stringWriter.toString());
    }

}
