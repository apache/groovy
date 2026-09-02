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

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Logger;

/**
 * Support class for creating hardened JAXP factories.
 * <p>
 * Every {@code create*} method returns a factory pre-configured to resist
 * common XML attack vectors (XXE, billion laughs, external resource
 * resolution). Overloads accepting flags let callers relax specific defaults
 * when they legitimately need DOCTYPE support or external resource resolution.
 */
public class FactorySupport {

    private static final Logger LOG = Logger.getLogger(FactorySupport.class.getName());

    private static final String DISALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    private static final String MAX_ELEMENT_DEPTH_LIMIT = "jdk.xml.maxElementDepth";

    /**
     * Default bound on how deeply elements may nest in a parsed document.
     * <p>
     * {@link XMLConstants#FEATURE_SECURE_PROCESSING} does not bound element depth: the JAXP
     * {@code jdk.xml.maxElementDepth} limit defaults to {@code 0}, meaning unlimited. The parse
     * itself survives an arbitrarily deep document, because SAX tracks nesting on the heap, but the
     * first consumer to walk the result recursively &mdash; {@code Node.text()},
     * {@code XmlNodePrinter}, {@code GPathResult.toString()}, {@code XmlUtil.serialize} &mdash;
     * runs one stack frame per level and dies with a {@link StackOverflowError}. That is an
     * {@link Error}, so it escapes the {@code catch (Exception)} an application would reasonably
     * use to handle a malformed document. Bounding the depth at parse time turns it into an
     * ordinary parse failure, at one check point, before any of those consumers is reached.
     * <p>
     * The value matches {@code groovy.json}'s nesting bound, and sits far above any realistic
     * document.
     *
     * @since 6.0.0
     */
    public static final int DEFAULT_MAX_ELEMENT_DEPTH = 1000;

    /**
     * Logs that a security-hardening setting could not be applied to a JAXP factory. These settings
     * are applied on a best-effort basis (a non-standard JAXP provider may not recognise them), but a
     * silent failure would leave the factory more permissive than the caller expects, so it is logged
     * rather than swallowed.
     *
     * @param factory the factory the setting was applied to
     * @param name    the feature/attribute/property name
     * @param value   the value that could not be applied
     * @param cause   the failure, or {@code null} if the value was simply not retained after being set
     */
    private static void warnHardeningNotApplied(Object factory, String name, Object value, Exception cause) {
        LOG.warning("Unable to apply XML security setting '" + name + "'=" + value + " on "
                + factory.getClass().getName()
                + (cause != null ? ": " + cause : " (value was not retained after being set)")
                + "; the factory may be more permissive than intended (e.g. weaker XXE/DTD protection).");
    }

    /**
     * Runs the supplied factory creation action and normalizes checked failures.
     *
     * @param action the action creating the factory instance
     * @return the created factory
     * @throws ParserConfigurationException if the factory cannot be configured
     */
    static Object createFactory(PrivilegedExceptionAction action) throws ParserConfigurationException {
        try {
            return action.run();
        } catch (ParserConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new hardened {@link DocumentBuilderFactory}.
     * <p>
     * Equivalent to {@link #createDocumentBuilderFactory(boolean) createDocumentBuilderFactory(false)}:
     * DOCTYPE declarations are rejected and {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * is enabled. Pass {@code true} to {@link #createDocumentBuilderFactory(boolean)}
     * if DOCTYPE support is required.
     * <p>
     * Note: prior to Groovy 6.0.0 this method returned a bare JDK factory with
     * no hardening applied. Callers that previously parsed DOCTYPE-bearing
     * documents through the returned factory must switch to
     * {@link #createDocumentBuilderFactory(boolean) createDocumentBuilderFactory(true)}.
     *
     * @return a newly created, hardened document builder factory
     * @throws ParserConfigurationException if the factory cannot be created
     */
    public static DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        return createDocumentBuilderFactory(false);
    }

    /**
     * Creates a new hardened {@link DocumentBuilderFactory}.
     * <p>
     * The returned factory has {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * enabled, the Apache {@code disallow-doctype-decl} feature toggled
     * according to the {@code allowDocTypeDeclaration} flag, XInclude disabled,
     * and entity reference expansion disabled.
     *
     * @param allowDocTypeDeclaration whether {@code DOCTYPE} declarations are
     *                                allowed in parsed documents (defaults
     *                                should be {@code false} for untrusted input)
     * @return a newly created, hardened document builder factory
     * @throws ParserConfigurationException if the factory cannot be created
     * @since 6.0.0
     */
    public static DocumentBuilderFactory createDocumentBuilderFactory(boolean allowDocTypeDeclaration) throws ParserConfigurationException {
        DocumentBuilderFactory factory = (DocumentBuilderFactory) createFactory(DocumentBuilderFactory::newInstance);
        setFeatureQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeatureQuietly(factory, DISALLOW_DOCTYPE_DECL_FEATURE, !allowDocTypeDeclaration);
        setMaxElementDepthQuietly(factory);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    /**
     * Creates a new hardened {@link SAXParserFactory}.
     * <p>
     * Equivalent to {@link #createSaxParserFactory(boolean) createSaxParserFactory(false)}:
     * DOCTYPE declarations are rejected and {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * is enabled. Pass {@code true} to {@link #createSaxParserFactory(boolean)}
     * if DOCTYPE support is required.
     * <p>
     * Note: prior to Groovy 6.0.0 this method returned a bare JDK factory with
     * no hardening applied. Callers that previously parsed DOCTYPE-bearing
     * documents through the returned factory must switch to
     * {@link #createSaxParserFactory(boolean) createSaxParserFactory(true)}.
     *
     * @return a newly created, hardened SAX parser factory
     * @throws ParserConfigurationException if the factory cannot be created
     */
    public static SAXParserFactory createSaxParserFactory() throws ParserConfigurationException {
        return createSaxParserFactory(false);
    }

    /**
     * Creates a new hardened {@link SAXParserFactory}.
     * <p>
     * The returned factory has {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * enabled and the Apache {@code disallow-doctype-decl} feature toggled
     * according to the {@code allowDocTypeDeclaration} flag.
     *
     * @param allowDocTypeDeclaration whether {@code DOCTYPE} declarations are
     *                                allowed in parsed documents (defaults
     *                                should be {@code false} for untrusted input)
     * @return a newly created, hardened SAX parser factory
     * @throws ParserConfigurationException if the factory cannot be created
     * @since 6.0.0
     */
    public static SAXParserFactory createSaxParserFactory(boolean allowDocTypeDeclaration) throws ParserConfigurationException {
        SAXParserFactory factory = (SAXParserFactory) createFactory(SAXParserFactory::newInstance);
        setFeatureQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeatureQuietly(factory, DISALLOW_DOCTYPE_DECL_FEATURE, !allowDocTypeDeclaration);
        return factory;
    }

    /**
     * Creates a new hardened {@link XMLInputFactory} for StAX parsing.
     * <p>
     * Equivalent to {@code createXMLInputFactory(false)}: DTD support and
     * external entity resolution are disabled.
     *
     * @return a newly created, hardened StAX input factory
     * @since 6.0.0
     */
    public static XMLInputFactory createXMLInputFactory() {
        return createXMLInputFactory(false);
    }

    /**
     * Creates a new hardened {@link XMLInputFactory} for StAX parsing.
     * <p>
     * The returned factory disables external entity resolution unconditionally
     * and toggles {@link XMLInputFactory#SUPPORT_DTD} according to the
     * {@code allowDocTypeDeclaration} flag.
     *
     * @param allowDocTypeDeclaration whether {@code DOCTYPE} declarations are
     *                                allowed in parsed documents (defaults
     *                                should be {@code false} for untrusted input)
     * @return a newly created, hardened StAX input factory
     * @since 6.0.0
     */
    public static XMLInputFactory createXMLInputFactory(boolean allowDocTypeDeclaration) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        setPropertyQuietly(factory, XMLInputFactory.SUPPORT_DTD, allowDocTypeDeclaration);
        setPropertyQuietly(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        setPropertyQuietly(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setPropertyQuietly(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    /**
     * Creates a new hardened {@link TransformerFactory}.
     * <p>
     * The returned factory has {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * enabled and the Apache {@code disallow-doctype-decl} feature toggled
     * according to the {@code allowDocTypeDeclaration} flag. Access to
     * external DTDs and stylesheets is blocked unless {@code allowExternalResources}
     * is {@code true}.
     *
     * @param allowDocTypeDeclaration whether {@code DOCTYPE} declarations are
     *                                allowed in transformed documents
     * @param allowExternalResources  whether {@code <xsl:import>}/{@code <xsl:include>}
     *                                may resolve external DTDs or stylesheets
     * @return a newly created, hardened transformer factory
     * @since 6.0.0
     */
    public static TransformerFactory createTransformerFactory(boolean allowDocTypeDeclaration, boolean allowExternalResources) {
        TransformerFactory factory = TransformerFactory.newInstance();
        setFeatureQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeatureQuietly(factory, DISALLOW_DOCTYPE_DECL_FEATURE, !allowDocTypeDeclaration);
        String externalAccess = allowExternalResources ? "all" : "";
        setAttributeQuietly(factory, XMLConstants.ACCESS_EXTERNAL_DTD, externalAccess);
        setAttributeQuietly(factory, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, externalAccess);
        return factory;
    }

    /**
     * Creates a new hardened {@link SchemaFactory} for the requested schema language.
     * <p>
     * The returned factory has {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * enabled, which by default already restricts resolution of external schemas
     * and DTDs. That default can be widened by a global
     * {@code javax.xml.accessExternalSchema}/{@code javax.xml.accessExternalDTD}
     * system property or a {@code jaxp.properties} entry; if you need external
     * access denied regardless of such global configuration, set
     * {@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} and {@link XMLConstants#ACCESS_EXTERNAL_DTD}
     * to {@code ""} on the returned factory (a property set directly on the factory takes
     * precedence over the global configuration).
     *
     * @param schemaLanguage the schema language URI (see {@link XMLConstants})
     * @return a newly created, hardened schema factory
     * @since 6.0.0
     */
    public static SchemaFactory createSchemaFactory(String schemaLanguage) {
        SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage);
        setFeatureQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setPropertyQuietly(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setPropertyQuietly(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    /**
     * Creates a new hardened {@link XPathFactory}.
     * <p>
     * The returned factory has {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * enabled.
     *
     * @return a newly created, hardened XPath factory
     * @since 6.0.0
     */
    public static XPathFactory createXPathFactory() {
        XPathFactory factory = XPathFactory.newInstance();
        setFeatureQuietly(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory;
    }

    /**
     * Creates a {@link SAXParser} from the supplied factory, bounded by
     * {@link #DEFAULT_MAX_ELEMENT_DEPTH}.
     * <p>
     * The depth limit cannot be set on a {@link SAXParserFactory} &mdash; it is a parser property
     * &mdash; so SAX callers should obtain their parser here rather than calling
     * {@link SAXParserFactory#newSAXParser()} directly, or the bound will not be applied.
     *
     * @param factory the factory to create the parser from
     * @return a newly created parser with the element-depth bound applied
     * @throws ParserConfigurationException if the parser cannot be created
     * @throws SAXException if the parser cannot be created
     * @since 6.0.0
     */
    public static SAXParser createSaxParser(SAXParserFactory factory) throws ParserConfigurationException, SAXException {
        SAXParser parser = factory.newSAXParser();
        setMaxElementDepthQuietly(parser);
        return parser;
    }

    /**
     * The element-depth bound to apply, or {@code null} to leave the parser's own default in place.
     * <p>
     * A value supplied through the standard {@code jdk.xml.maxElementDepth} system property is the
     * user's choice of bound, and JAXP would let an explicitly set parser property override it, so
     * nothing is applied when that property is present &mdash; including a deliberate {@code 0},
     * which restores unlimited depth.
     */
    private static String maxElementDepthLimit() {
        return System.getProperty(MAX_ELEMENT_DEPTH_LIMIT) == null
                ? Integer.toString(DEFAULT_MAX_ELEMENT_DEPTH)
                : null;
    }

    private static void setMaxElementDepthQuietly(DocumentBuilderFactory factory) {
        String limit = maxElementDepthLimit();
        if (limit == null) {
            return;
        }
        try {
            factory.setAttribute(MAX_ELEMENT_DEPTH_LIMIT, limit);
        } catch (IllegalArgumentException e) {
            warnHardeningNotApplied(factory, MAX_ELEMENT_DEPTH_LIMIT, limit, e);
        }
    }

    private static void setMaxElementDepthQuietly(SAXParser parser) {
        String limit = maxElementDepthLimit();
        if (limit == null) {
            return;
        }
        try {
            parser.setProperty(MAX_ELEMENT_DEPTH_LIMIT, limit);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            warnHardeningNotApplied(parser, MAX_ELEMENT_DEPTH_LIMIT, limit, e);
        }
    }

    // package-private for testing
    static void setFeatureQuietly(DocumentBuilderFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
            if (factory.getFeature(feature) != value) {
                warnHardeningNotApplied(factory, feature, value, null);
            }
        } catch (ParserConfigurationException e) {
            warnHardeningNotApplied(factory, feature, value, e);
        }
    }

    private static void setFeatureQuietly(SAXParserFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
            if (factory.getFeature(feature) != value) {
                warnHardeningNotApplied(factory, feature, value, null);
            }
        } catch (ParserConfigurationException | SAXNotSupportedException | SAXNotRecognizedException e) {
            warnHardeningNotApplied(factory, feature, value, e);
        }
    }

    private static void setFeatureQuietly(TransformerFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
            if (factory.getFeature(feature) != value) {
                warnHardeningNotApplied(factory, feature, value, null);
            }
        } catch (TransformerConfigurationException e) {
            warnHardeningNotApplied(factory, feature, value, e);
        }
    }

    private static void setFeatureQuietly(SchemaFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
            if (factory.getFeature(feature) != value) {
                warnHardeningNotApplied(factory, feature, value, null);
            }
        } catch (SAXNotSupportedException | SAXNotRecognizedException e) {
            warnHardeningNotApplied(factory, feature, value, e);
        }
    }

    private static void setFeatureQuietly(XPathFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
            if (factory.getFeature(feature) != value) {
                warnHardeningNotApplied(factory, feature, value, null);
            }
        } catch (XPathFactoryConfigurationException e) {
            warnHardeningNotApplied(factory, feature, value, e);
        }
    }

    private static void setAttributeQuietly(TransformerFactory factory, String attribute, Object value) {
        try {
            factory.setAttribute(attribute, value);
        } catch (IllegalArgumentException e) {
            warnHardeningNotApplied(factory, attribute, value, e);
        }
    }

    private static void setPropertyQuietly(XMLInputFactory factory, String property, Object value) {
        try {
            factory.setProperty(property, value);
        } catch (IllegalArgumentException e) {
            warnHardeningNotApplied(factory, property, value, e);
        }
    }

    private static void setPropertyQuietly(SchemaFactory factory, String property, Object value) {
        try {
            factory.setProperty(property, value);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            warnHardeningNotApplied(factory, property, value, e);
        }
    }
}
