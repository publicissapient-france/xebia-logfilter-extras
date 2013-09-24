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

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 */
public class HttpServletRequestLoggingWrapperTest {


    @Test
    public void should_read_full_body_and_return_it() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getInputStream()).thenReturn(getStreamForString(SMALL_PAYLOAD));
        HttpServletRequestLoggingWrapper wrapper = new HttpServletRequestLoggingWrapper(request, 12);
        Assert.assertEquals("Body of the Request and result body should match", SMALL_PAYLOAD, wrapper.getBody());
    }

    @Test
    public void should_read_empty_body_and_return_it() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getInputStream()).thenReturn(getStreamForString(null));
        HttpServletRequestLoggingWrapper wrapper = new HttpServletRequestLoggingWrapper(request, 12);
        Assert.assertEquals("Body of the Request and result body should match", "", wrapper.getBody());
    }


    @Test
    public void should_give_a_stream_containing_the_body() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getInputStream()).thenReturn(getStreamForString(SMALL_PAYLOAD));
        HttpServletRequestLoggingWrapper wrapper = new HttpServletRequestLoggingWrapper(request, 12);
        byte[] bytes = new byte[100];
        int ln = wrapper.getInputStream().read(bytes);
        Assert.assertEquals("Body of the Request and result body should match", SMALL_PAYLOAD, new String(bytes, 0, ln));
    }

    @Test(expected = IOException.class)
    public void should_throw_exception_caught_during_load() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getInputStream()).thenThrow(new IOException("Fail"));
        HttpServletRequestLoggingWrapper wrapper = new HttpServletRequestLoggingWrapper(request, 12);
        wrapper.getInputStream();
        Assert.fail("An IOException should occur when getting the stream");
    }

    @Test
    public void should_limit_caught_body_to_maxDump_but_not_limit_stream() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getInputStream()).thenReturn(getStreamForString(HUGE_PAYLOAD));
        HttpServletRequestLoggingWrapper wrapper = new HttpServletRequestLoggingWrapper(request, 1);
        String body = wrapper.getBody();
        Assert.assertTrue("Body String should be limited", body.contains("-- 1880 more bytes skipped from dump by max dump size limit"));

        byte[] bytes = new byte[3000];
        int ln = wrapper.getInputStream().read(bytes);
        Assert.assertEquals("Wrapper should give complete stream", HUGE_PAYLOAD, new String(bytes, 0, ln));
    }

    ServletInputStream getStreamForString(String payloadString){
        final ByteArrayInputStream payload = new ByteArrayInputStream(payloadString != null ? payloadString.getBytes(): new byte[0]);

        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return payload.read();
            }
        };
    }


    static final String SMALL_PAYLOAD = "My Payload ...";
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
