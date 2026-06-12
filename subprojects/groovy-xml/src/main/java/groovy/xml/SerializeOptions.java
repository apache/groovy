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

import java.nio.charset.Charset;

/**
 * Options for controlling XML serialization via {@link XmlUtil#serialize}.
 * <p>
 * All options have sensible defaults matching the existing behaviour of
 * {@code XmlUtil.serialize()}, so only options that need to differ from
 * the defaults need to be specified:
 * <pre>
 * // Groovy named parameters:
 * XmlUtil.serialize(node, new SerializeOptions(encoding: 'ISO-8859-1', indent: 4))
 * </pre>
 *
 * @since 6.0.0
 */
public class SerializeOptions {

    private String encoding = "UTF-8";
    private int indent = 2;
    private boolean allowDocTypeDeclaration = false;
    private boolean allowExternalResources = false;

    /**
     * Creates a new options instance using the default serialization settings.
     */
    public SerializeOptions() {
    }

    /**
     * The character encoding for the XML output.
     * Default is {@code "UTF-8"}.
     *
     * @return the encoding name
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding for the XML output.
     *
     * @param encoding the encoding name to use
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * The character encoding as a {@link Charset}.
     *
     * @return the Charset for the configured encoding
     */
    public Charset getCharset() {
        return Charset.forName(encoding);
    }

    /**
     * The number of spaces to use for indentation.
     * Default is {@code 2}.
     *
     * @return the indent amount
     */
    public int getIndent() {
        return indent;
    }

    /**
     * Sets the number of spaces to use for indentation.
     *
     * @param indent the indent amount
     */
    public void setIndent(int indent) {
        this.indent = indent;
    }

    /**
     * Whether to allow DOCTYPE declarations during serialization.
     * Default is {@code false}.
     *
     * @return true if DOCTYPE declarations are allowed
     */
    public boolean isAllowDocTypeDeclaration() {
        return allowDocTypeDeclaration;
    }

    /**
     * Sets whether DOCTYPE declarations are allowed during serialization.
     *
     * @param allowDocTypeDeclaration {@code true} to allow DOCTYPE declarations
     */
    public void setAllowDocTypeDeclaration(boolean allowDocTypeDeclaration) {
        this.allowDocTypeDeclaration = allowDocTypeDeclaration;
    }

    /**
     * Whether the underlying {@link javax.xml.transform.TransformerFactory}
     * may resolve external DTDs and stylesheets (e.g. via {@code <xsl:import>}
     * or {@code <xsl:include>}).
     * Default is {@code false}; set to {@code true} when serializing XSLT
     * documents that legitimately reference external resources.
     *
     * @return {@code true} if external resource resolution is allowed
     * @since 6.0.0
     */
    public boolean isAllowExternalResources() {
        return allowExternalResources;
    }

    /**
     * Sets whether external DTDs and stylesheets may be resolved during
     * serialization.
     *
     * @param allowExternalResources {@code true} to allow external resource resolution
     * @since 6.0.0
     */
    public void setAllowExternalResources(boolean allowExternalResources) {
        this.allowExternalResources = allowExternalResources;
    }

    /**
     * Shared default serialization options.
     */
    static final SerializeOptions DEFAULT = new SerializeOptions();
}
