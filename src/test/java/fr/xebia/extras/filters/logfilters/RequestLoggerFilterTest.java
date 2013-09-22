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

import javax.servlet.FilterConfig;

/**
 *
 */
public class RequestLoggerFilterTest {


    public static final String MAX_DUMP_SIZE_IN_KB = "maxDumpSizeInKB";

    @Test
    public void maxDumpSizeInKB_should_be_parsed_from_init_config() throws Exception {
        FilterConfig config = Mockito.mock(FilterConfig.class);
        Mockito.when(config.getInitParameter(MAX_DUMP_SIZE_IN_KB)).thenReturn("42");

        RequestLoggerFilter filter = new RequestLoggerFilter();

        filter.init(config);

        Assert.assertEquals(42, filter.getMaxDumpSizeInKB());
        Mockito.verify(config).getInitParameter(MAX_DUMP_SIZE_IN_KB);
    }



    @Test
    public void maxDumpSizeInKB_should_be_defaulted_when_init_config_is_not_an_integer() throws Exception {
        FilterConfig config = Mockito.mock(FilterConfig.class);
        Mockito.when(config.getInitParameter(MAX_DUMP_SIZE_IN_KB)).thenReturn("42A");

        RequestLoggerFilter filter = new RequestLoggerFilter();

        filter.init(config);

        Assert.assertEquals(500, filter.getMaxDumpSizeInKB());
        Mockito.verify(config).getInitParameter(MAX_DUMP_SIZE_IN_KB);
    }

    @Test
    public void maxDumpSizeInKB_should_be_defaulted_when_init_config_is_not_a_positive_integer() throws Exception {
        FilterConfig config = Mockito.mock(FilterConfig.class);
        Mockito.when(config.getInitParameter(MAX_DUMP_SIZE_IN_KB)).thenReturn("0");

        RequestLoggerFilter filter = new RequestLoggerFilter();

        filter.init(config);

        Assert.assertEquals(500, filter.getMaxDumpSizeInKB());
        Mockito.verify(config).getInitParameter(MAX_DUMP_SIZE_IN_KB);
    }

    @Test
    public void testDoFilter() throws Exception {

    }
}
