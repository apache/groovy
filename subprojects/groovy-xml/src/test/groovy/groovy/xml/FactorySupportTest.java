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
package groovy.xml;

import junit.framework.TestCase;

import javax.xml.parsers.ParserConfigurationException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class FactorySupportTest extends TestCase {
    private static final PrivilegedActionException PRIVILEGED_ACTION_EXCEPTION = new PrivilegedActionException(new IllegalStateException());
    private static final ParserConfigurationException PARSER_CONFIGURATION_EXCEPTION = new ParserConfigurationException();

    public void testCreatesFactories() throws Exception {
        assertNotNull(FactorySupport.createDocumentBuilderFactory());
        assertNotNull(FactorySupport.createSaxParserFactory());
    }

    public void testParserConfigurationExceptionNotWrapped() throws ParserConfigurationException {
        try {
            FactorySupport.createFactory(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    throw PARSER_CONFIGURATION_EXCEPTION;
                }
            });
            fail("Exception was not caught");
        } catch (Throwable t) {
            assertSame(PARSER_CONFIGURATION_EXCEPTION, t);
        }
    }

    public void testOtherExceptionsWrappedAsUnchecked() throws ParserConfigurationException {
        try {
            FactorySupport.createFactory(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    throw PRIVILEGED_ACTION_EXCEPTION;
                }
            });
            fail("Exception was not caught");
        } catch (RuntimeException re) {
            assertSame(PRIVILEGED_ACTION_EXCEPTION, re.getCause());
        } catch (Throwable t) {
            fail("Exception was not wrapped as runtime");
        }
    }
}