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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class HttpServletResponseLoggingWrapperTest {


    public static final String MY_PAYLOAD = "My payload";

    @Test
    public void should_intercept_set_header_and_pass_to_response() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(response, 1);

        wrapper.setHeader("TEST", "first value");
        wrapper.setHeader("TEST", "second value");
        wrapper.setHeader("TESTBis", "value");

        // Verify interception
        Map<String, List<String>> headers =  wrapper.getHeaders();
        Assert.assertEquals("2 headers have been added", 2, headers.size());
        Assert.assertEquals("first value", headers.get("TEST").get(0));
        Assert.assertEquals("second value", headers.get("TEST").get(1));
        Assert.assertEquals("value", headers.get("TESTBis").get(0));

        // Verify real call to original response object
        Mockito.verify(response).setHeader("TEST", "first value");
        Mockito.verify(response).setHeader("TEST", "second value");
        Mockito.verify(response).setHeader("TESTBis", "value");
    }

    @Test
    public void should_intercept_addHeader_and_pass_to_response() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(response, 1);

        wrapper.addHeader("TEST", "first value");
        wrapper.addHeader("TESTBis", "value");

        // Verify interception
        Map<String, List<String>> headers =  wrapper.getHeaders();
        Assert.assertEquals("2 headers have been added", 2, headers.size());
        Assert.assertEquals("first value", headers.get("TEST").get(0));
        Assert.assertEquals("value", headers.get("TESTBis").get(0));

        // Verify real call to original response object
        Mockito.verify(response).addHeader("TEST", "first value");
        Mockito.verify(response).addHeader("TESTBis", "value");
    }

    @Test
    public void flush_buffer_should_call_underlying_flush_buffer() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(response, 1);
        wrapper.flushBuffer();
        Mockito.verify(response).flushBuffer();
    }

    @Test
    public void getOutputStream_should_return_a_ServletOutputStream_intercepting_content() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        final ByteArrayOutputStream contentWritten = new ByteArrayOutputStream();
        ServletOutputStream originStream =  new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                contentWritten.write(b);
            }
        };
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        Mockito.when(originResponse.getOutputStream()).thenReturn(originStream);

        ServletOutputStream stream =  wrapper.getOutputStream();
        stream.print(MY_PAYLOAD);

        Assert.assertEquals("Written content and wrapper content should be the same", MY_PAYLOAD,wrapper.getContentAsInputString());
        Mockito.verify(originResponse).getOutputStream();
        Assert.assertArrayEquals("Written content and origin written content should be the same", MY_PAYLOAD.getBytes(), contentWritten.toByteArray());
    }

    @Test(expected = IllegalStateException.class)
    public void getOutputStream_should_throw_illegalException_when_getWriter_already_called() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream originStream = Mockito.mock(ServletOutputStream.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);
        Mockito.when(originResponse.getOutputStream()).thenReturn(originStream);

        wrapper.getWriter();
        ServletOutputStream stream =  wrapper.getOutputStream();
        Assert.fail("An IllegalStateException should have occured");
    }

    @Test(expected = IOException.class)
    public void getOutputStream_should_throw_IOException_thrown_from_origin_stream() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream originStream = Mockito.mock(ServletOutputStream.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);
        Mockito.when(originResponse.getOutputStream()).thenThrow(new IOException("Fail"));

        ServletOutputStream stream =  wrapper.getOutputStream();

        Assert.fail("An IOException should have occured");
    }

    @Test
    public void getOutputStream_should_return_same_stream_each_time() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream originStream = Mockito.mock(ServletOutputStream.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);
        Mockito.when(originResponse.getOutputStream()).thenReturn(originStream);

        ServletOutputStream stream =  wrapper.getOutputStream();

        Assert.assertTrue(stream == wrapper.getOutputStream());
    }

    @Test
    public void getWriter_should_return_a_PrintWriter_intercepting_content() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        final ByteArrayOutputStream contentWritten = new ByteArrayOutputStream();
        ServletOutputStream originStream =  new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                contentWritten.write(b);
            }
        };
        PrintWriter originWriter = Mockito.mock(PrintWriter.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        Mockito.when(originResponse.getWriter()).thenReturn(originWriter);
        Mockito.when(originResponse.getOutputStream()).thenReturn(originStream);
        Mockito.when(originResponse.getCharacterEncoding()).thenReturn("UTF-8");

        PrintWriter writer =  wrapper.getWriter();
        writer.print(MY_PAYLOAD);
        writer.flush();

        Assert.assertEquals("Written content and wrapper content should be the same", MY_PAYLOAD,wrapper.getContentAsInputString());
        Mockito.verify(originResponse).getOutputStream();
        Assert.assertArrayEquals("Written content and origin written content should be the same", MY_PAYLOAD.getBytes(), contentWritten.toByteArray());

    }

    @Test(expected = IllegalStateException.class)
    public void getWriter_should_throw_illegalException_when_getOutputStream_already_called() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        PrintWriter originWriter = Mockito.mock(PrintWriter.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);
        Mockito.when(originResponse.getWriter()).thenReturn(originWriter);

        wrapper.getOutputStream();
        PrintWriter stream =  wrapper.getWriter();
        Assert.fail("An IllegalStateException should have occured");
    }

    @Test(expected = IOException.class)
    public void getWriter_should_throw_IOException_thrown_from_origin_stream() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        PrintWriter originWriter = Mockito.mock(PrintWriter.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);
        Mockito.when(originResponse.getOutputStream()).thenThrow(new IOException("Fail"));

        PrintWriter writer =  wrapper.getWriter();

        Assert.fail("An IOException should have occured");
    }

    @Test
    public void getWriter_should_return_same_stream_each_time() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        PrintWriter originWriter = Mockito.mock(PrintWriter.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);
        Mockito.when(originResponse.getWriter()).thenReturn(originWriter);

        PrintWriter writer =  wrapper.getWriter();

        Assert.assertTrue(writer == wrapper.getWriter());
    }


    @Test
    public void contentAsInputString_should_return_String_wrote_to_origin_response() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        final ByteArrayOutputStream contentWritten = new ByteArrayOutputStream();
        ServletOutputStream originStream =  new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                contentWritten.write(b);
            }
        };
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        Mockito.when(originResponse.getOutputStream()).thenReturn(originStream);

        ServletOutputStream stream =  wrapper.getOutputStream();
        stream.print(MY_PAYLOAD);

        Assert.assertEquals("Written content and wrapper content should be the same", MY_PAYLOAD,wrapper.getContentAsInputString());
        Assert.assertArrayEquals("Written content and origin written content should be the same", MY_PAYLOAD.getBytes(), contentWritten.toByteArray());
    }

    @Test
    public void contentAsInputString_should_return_String_wrote_to_origin_response_limited_to_maxDumpSize() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        final ByteArrayOutputStream contentWritten = new ByteArrayOutputStream();
        ServletOutputStream originStream =  new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                contentWritten.write(b);
            }
        };        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        Mockito.when(originResponse.getOutputStream()).thenReturn(originStream);

        ServletOutputStream stream =  wrapper.getOutputStream();
        stream.print(HUGE_PAYLOAD);

        Assert.assertTrue("Body String should be limited", wrapper.getContentAsInputString().contains("-- 1880 more bytes skipped from dump by max dump size limit"));
        Assert.assertArrayEquals("Written content and origin written content should be the same", HUGE_PAYLOAD.getBytes(), contentWritten.toByteArray());
    }




    @Test
    public void contentAsBytes_should_return_String_wrote_to_origin_response_in_bytes_with_no_limit() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);
        final ByteArrayOutputStream contentWritten = new ByteArrayOutputStream();
        ServletOutputStream originStream =  new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                contentWritten.write(b);
            }
        };
        Mockito.when(originResponse.getOutputStream()).thenReturn(originStream);

        ServletOutputStream stream =  wrapper.getOutputStream();
        stream.print(HUGE_PAYLOAD);

        Assert.assertArrayEquals("Written content and wrapper content should be the same", HUGE_PAYLOAD.getBytes(), wrapper.getContentAsBytes());
        Assert.assertArrayEquals("Written content and origin written content should be the same", HUGE_PAYLOAD.getBytes(), contentWritten.toByteArray());

    }

    @Test
    public void sendError_should_intercept_status_code_and_pass_it_to_origin() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        wrapper.sendError(404);

        Mockito.verify(originResponse).sendError(404);
        Assert.assertEquals("404 Error", wrapper.getStatusCode());
    }

    @Test
    public void sendError_should_intercept_status_code_plus_string_and_pass_it_to_origin() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        wrapper.sendError(404, "Not Found");

        Mockito.verify(originResponse).sendError(404, "Not Found");
        Assert.assertEquals("404 Not Found", wrapper.getStatusCode());
    }

    @Test
    public void sendRedirect_should_intercept_status_code_and_redirect_to_origin() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        wrapper.sendRedirect("Location");

        Mockito.verify(originResponse).sendRedirect("Location");
        Assert.assertEquals("302 Redirect", wrapper.getStatusCode());
        Map<String, List<String>> headers =  wrapper.getHeaders();
        Assert.assertEquals("Location", headers.get("Location").get(0));
    }


    @Test
    public void setStatus_should_intercept_status_and_pass_it_to_origin() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        wrapper.setStatus(200);

        Mockito.verify(originResponse).setStatus(200);
        Assert.assertEquals("200", wrapper.getStatusCode());
    }

    @Test
    public void setStatus_should_intercept_status_plus_string_and_pass_it_to_origin() throws Exception {
        HttpServletResponse originResponse = Mockito.mock(HttpServletResponse.class);
        HttpServletResponseLoggingWrapper wrapper = new HttpServletResponseLoggingWrapper(originResponse, 1);

        wrapper.setStatus(200, "OK");

        Mockito.verify(originResponse).setStatus(200, "OK");
        Assert.assertEquals("200 OK", wrapper.getStatusCode());
    }


    static final String HUGE_PAYLOAD = "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################" +
            "################################################################################";



}
