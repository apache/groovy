/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.control.io;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Janitor;

import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

/**
 * Unit test for AbstractReaderSource.
 */
public class AbstractReaderSourceTest {

    private static final String SAMPLE_SOURCE_LINE1 = "def x = 1;";
    private static final String SAMPLE_SOURCE_LINE2 = "def y = 2;";
    private static final String SAMPLE_SOURCE_LINE3 = "x+y";
    private AbstractReaderSourceSubclass readerSource;
    private Janitor janitor;

    @Before
    public void setUp() throws Exception {
        readerSource = new AbstractReaderSourceSubclass();
        janitor = new Janitor();
    }


    @Test
    public void testGetLine() {

        final String line0 = readerSource.getLine(0, janitor);
        final String line1 = readerSource.getLine(1, janitor);
        final String line2 = readerSource.getLine(2, janitor);
        final String line3 = readerSource.getLine(3, janitor);
        final String line4 = readerSource.getLine(4, janitor);

        Assert.assertNull("out of bounds request should return null", line0);
        Assert.assertEquals("Wrong source line", SAMPLE_SOURCE_LINE1, line1);
        Assert.assertEquals("Wrong source line", SAMPLE_SOURCE_LINE2, line2);
        Assert.assertEquals("Wrong source line", SAMPLE_SOURCE_LINE3, line3);
        Assert.assertNull("out of bounds request should return null", line4);
    }

    @Test
    public void testGetLine_NullJanitor() {

        final String line = readerSource.getLine(1, null);

        Assert.assertEquals("Wrong source line", SAMPLE_SOURCE_LINE1, line);
    }

    /**
     * This is a test specific subclass for AbstractReaderSource.
     */
    private static class AbstractReaderSourceSubclass extends AbstractReaderSource {

        private AbstractReaderSourceSubclass() {
            super(new CompilerConfiguration());
        }

        public Reader getReader() throws IOException {
            return new StringReader(
                    String.format("%s\n%s\n%s", SAMPLE_SOURCE_LINE1, SAMPLE_SOURCE_LINE2, SAMPLE_SOURCE_LINE3)
            );
        }

        public URI getURI() {
            return null;
        }
    }
}