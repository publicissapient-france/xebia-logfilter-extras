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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: slemesle
* Date: 20/09/13
* Time: 10:33
* To change this template use File | Settings | File Templates.
*/
class HttpServletResponseLoggingWrapper extends HttpServletResponseWrapper {


    public static final String ISO_8859_1 = "ISO-8859-1";
    private final int maxDumpSizeInB;
    protected PrintWriter writer = null;
    protected CacheResponseStream cache = null;
    protected Map<String, List<String>> headers = new HashMap<>();
    private final HttpServletResponse response;
    String status = "200 OK";

    Map<String, List<String>> getHeaders() {
        return headers;
    }

    HttpServletResponseLoggingWrapper(final HttpServletResponse response, final int _maxDumpSizeInKB ) {
        super(response);
        this.response = response;
        maxDumpSizeInB = _maxDumpSizeInKB * 1000;
    }

    @Override
    public void setHeader(final String name, final String value) {
        List<String> values = getHeaderValues(name);
        values.add(value);
        super.setHeader(name, value);
    }

    /**
     * Convenience method to get a header value list, initialize it and put it in the headers map
     * if it does not exist
     * @param name      header name
     * @return          list containing values for this header
     */
    private List<String> getHeaderValues(String name) {
        List<String> values = headers.get(name);
        if (values == null){
            values = new ArrayList<>();
            headers.put(name, values);
        }
        return values;
    }

    @Override
    public void addHeader(final String name, final String value) {
        List<String> values = getHeaderValues(name);
        values.add(value);
        super.addHeader(name, value);
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null){
            writer.flush();
        }
        if (cache != null){
            cache.flush();
        }
        super.flushBuffer();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getOutputStream() has already been called!");
        }
        if (cache == null) {
            cache = new CacheResponseStream(response.getOutputStream());
        }
        return cache;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }
        if (cache != null) {
            throw new IllegalStateException("getOutputStream() has already been called!");
        }

        cache = new CacheResponseStream(response.getOutputStream());
        String encoding = null;
        if (response.getCharacterEncoding() != null && Charset.isSupported(response.getCharacterEncoding())){
            encoding = response.getCharacterEncoding();
        } else {
            encoding = ISO_8859_1;
        }
        writer = new PrintWriter(new OutputStreamWriter(cache, encoding));
        return writer;
    }

    public String getContentAsInputString() {
        final byte [] bytes = cache.toByteArray();
        String body = null;
        if(bytes.length > maxDumpSizeInB){
            body = new StringBuilder(new String(bytes, 0, maxDumpSizeInB))
                    .append("\n-- ").append(bytes.length - maxDumpSizeInB)
                    .append(" more bytes skipped from dump by max dump size limit").toString();
        } else {
            body = new String(bytes, 0, bytes.length);
        }
        return body;
    }

    public byte[] getContentAsBytes() {
        return cache.toByteArray();
    }

    public String getStatusCode() {
        return status;
    }

    @Override
    public void sendError(final int sc, final String msg) throws IOException {
        super.sendError(sc, msg);
        status = sc + " " + msg;
    }

    @Override
    public void sendError(final int sc) throws IOException {
        super.sendError(sc);
        status = sc + " Error";
    }

    @Override
    public void sendRedirect(final String location) throws IOException {
        super.sendRedirect(location);
        List<String> values = getHeaderValues("Location");
        values.add(location);
        status = "302 Redirect";
    }

    @Override
    public void setStatus(final int sc) {
        super.setStatus(sc);
        status = sc +"";
    }

    @Override
    public void setStatus(final int sc, final String sm) {
        super.setStatus(sc, sm);
        status = sc + " " + sm;
    }


   private class CacheResponseStream extends ServletOutputStream {
        protected final ServletOutputStream outputStream;
        protected final ByteArrayOutputStream cache;

        public CacheResponseStream(final ServletOutputStream outputStream) {
            this.outputStream = outputStream;
            cache = new ByteArrayOutputStream(RequestLoggerFilter.BUFFER_SIZE);
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }

        @Override
        public void flush() throws IOException {
            outputStream.flush();
        }

        @Override
        public void write(final int i) throws IOException {
            outputStream.write(i);
            cache.write((byte) i);
        }


        public byte[] toByteArray() {
            return cache.toByteArray();
        }
    }
}
