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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used by the RequestLoggerFilter to dump request body into a byte buffer.
 * The byte buffer is then used to build a ServletInputStream for the HttpServletRequest it wraps.
 */
class HttpServletRequestLoggingWrapper extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestLoggingWrapper.class);
    private final ServletInputStream inputStream;
    private final ByteArrayOutputStream buffer;
    private final int maxDumpSizeInKB;
    private IOException caughtExceptionOnRead = null;

    public HttpServletRequestLoggingWrapper(final HttpServletRequest servletRequest, final int _maxDumpSizeInKB) {
        super(servletRequest);

        maxDumpSizeInKB = _maxDumpSizeInKB * 1000;
        buffer = new ByteArrayOutputStream(RequestLoggerFilter.BUFFER_SIZE);
        try (InputStream stream = super.getInputStream()) {
            int n;
            while ((n = stream.read()) != -1) {
                buffer.write(n);
            }
        } catch (final IOException e) {
            logger.error("IO caught while dumping request", e);
            caughtExceptionOnRead = e;
        }


        inputStream = new ServletInputStream() {
            final ByteArrayInputStream stream = new ByteArrayInputStream(buffer.toByteArray());

            @Override
            public int read() throws IOException {
                return stream.read();
            }
        };
    }

    String getBody() {
        String body = null;
        final byte[] bytes = buffer.toByteArray();
        if (buffer.size() > maxDumpSizeInKB) {
            body = new StringBuilder(new String(bytes, 0, maxDumpSizeInKB))
                    .append("\n-- ").append(buffer.size() - maxDumpSizeInKB)
                    .append(" more bytes skipped from dump by max dump size limit").toString();
        } else {
            body = new String(bytes, 0, bytes.length);
        }

        return body;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (caughtExceptionOnRead != null) {
            throw new IOException("Error occured while reading request body", caughtExceptionOnRead);
        }
        return inputStream;
    }

}
