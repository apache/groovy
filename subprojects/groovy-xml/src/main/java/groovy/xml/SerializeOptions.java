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

    public void setAllowDocTypeDeclaration(boolean allowDocTypeDeclaration) {
        this.allowDocTypeDeclaration = allowDocTypeDeclaration;
    }

    static final SerializeOptions DEFAULT = new SerializeOptions();
}
