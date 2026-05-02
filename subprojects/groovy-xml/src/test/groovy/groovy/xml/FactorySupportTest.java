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

import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FactorySupportTest {
    private static final PrivilegedActionException PRIVILEGED_ACTION_EXCEPTION = new PrivilegedActionException(new IllegalStateException());
    private static final ParserConfigurationException PARSER_CONFIGURATION_EXCEPTION = new ParserConfigurationException();
    private static final String DISALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    @Test
    public void createsFactories() throws Exception {
        assertNotNull(FactorySupport.createDocumentBuilderFactory());
        assertNotNull(FactorySupport.createSaxParserFactory());
    }

    @Test
    public void parserConfigurationExceptionNotWrapped() throws ParserConfigurationException {
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

    @Test
    public void otherExceptionsWrappedAsUnchecked() throws ParserConfigurationException {
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

    @Test
    public void zeroArgDocumentBuilderFactoryIsHardened() throws Exception {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory();
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertTrue(factory.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE));
    }

    @Test
    public void zeroArgSaxParserFactoryIsHardened() throws Exception {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory();
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertTrue(factory.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE));
    }

    @Test
    public void hardenedDocumentBuilderFactoryDisallowsDoctypeByDefault() throws Exception {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory(false);
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertTrue(factory.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE));
        assertFalse(factory.isXIncludeAware());
        assertFalse(factory.isExpandEntityReferences());
    }

    @Test
    public void hardenedDocumentBuilderFactoryAllowsDoctypeWhenRequested() throws Exception {
        DocumentBuilderFactory factory = FactorySupport.createDocumentBuilderFactory(true);
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertFalse(factory.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE));
    }

    @Test
    public void hardenedSaxParserFactoryDisallowsDoctypeByDefault() throws Exception {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory(false);
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertTrue(factory.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE));
    }

    @Test
    public void hardenedSaxParserFactoryAllowsDoctypeWhenRequested() throws Exception {
        SAXParserFactory factory = FactorySupport.createSaxParserFactory(true);
        assertFalse(factory.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE));
    }

    @Test
    public void hardenedXMLInputFactoryDisablesDtdAndExternalEntitiesByDefault() {
        XMLInputFactory factory = FactorySupport.createXMLInputFactory();
        assertEquals(Boolean.FALSE, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(Boolean.FALSE, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    @Test
    public void hardenedXMLInputFactoryAllowsDtdWhenRequestedButKeepsExternalEntitiesOff() {
        XMLInputFactory factory = FactorySupport.createXMLInputFactory(true);
        assertEquals(Boolean.TRUE, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(Boolean.FALSE, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    @Test
    public void hardenedTransformerFactoryBlocksExternalResourcesByDefault() {
        TransformerFactory factory = FactorySupport.createTransformerFactory(false, false);
        assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
        assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
    }

    @Test
    public void hardenedTransformerFactoryAllowsExternalResourcesWhenRequested() {
        TransformerFactory factory = FactorySupport.createTransformerFactory(true, true);
        assertEquals("all", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
        assertEquals("all", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
    }

    @Test
    public void hardenedSchemaFactoryEnablesSecureProcessing() throws Exception {
        SchemaFactory factory = FactorySupport.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    }

    @Test
    public void hardenedXPathFactoryEnablesSecureProcessing() throws Exception {
        XPathFactory factory = FactorySupport.createXPathFactory();
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    }
}
